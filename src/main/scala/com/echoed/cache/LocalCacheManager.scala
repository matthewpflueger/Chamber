package com.echoed.cache

import scala.collection.JavaConversions._
import com.google.common.collect.MapMaker
import org.slf4j.LoggerFactory
import scala.collection.mutable.ConcurrentMap
import java.util.{HashMap => JMap}
import scala.reflect.BeanProperty
import com.google.common.cache.{RemovalCause, RemovalNotification, RemovalListener, CacheBuilder}
import java.util.concurrent.{ThreadFactory, Executors, TimeUnit}

class LocalCacheManager extends CacheManager {

    private val logger = LoggerFactory.getLogger(classOf[LocalCacheManager])

    @BeanProperty var expireInMinutes = 30
    @BeanProperty var expirationConfig = new JMap[String, String]()

    @BeanProperty var cleanUpCachesEverySeconds = 10

    private val caches: ConcurrentMap[String, ConcurrentMap[String, AnyRef]] =
                new MapMaker().concurrencyLevel(4).makeMap[String, ConcurrentMap[String, AnyRef]]()

    Executors.newScheduledThreadPool(1, new ThreadFactory() {
        def newThread(r: Runnable) = new Thread(r, "LocalCacheManager")
    }).scheduleAtFixedRate(new Runnable {
        def run() {
            for (k <- caches.keys) caches(k).asInstanceOf[com.google.common.cache.Cache[Any, Any]].cleanUp
        }
    }, 60, cleanUpCachesEverySeconds, TimeUnit.SECONDS)

    def getCache[V <: AnyRef](cacheName: String, cacheListener: Option[CacheListener] = None) = {
        caches.getOrElseUpdate(cacheName, {
            val builder = CacheBuilder
                    .newBuilder()
                    .concurrencyLevel(4)
                    .softValues
                    .removalListener(new RemovalListenerProxy(cacheListener.getOrElse(NoOpCacheListener)))

            val expires =
                if (expirationConfig.containsKey(cacheName))
                    java.lang.Integer.parseInt(expirationConfig.get(cacheName))
                else expireInMinutes
            if (expires > 0) builder.expireAfterAccess(expireInMinutes, TimeUnit.MINUTES)

            logger.debug("Built cache {} with expires {}", cacheName, expires)
            builder.build[String, AnyRef].asMap
        }).asInstanceOf[ConcurrentMap[String, V]]
    }
}


object NoOpCacheListener extends CacheListener {
    def onRemoval(key: String, value: AnyRef, cause: String) {
        //no op
    }
}

class RemovalListenerProxy(cacheListener: CacheListener) extends RemovalListener[String, AnyRef] {
    def onRemoval(notification: RemovalNotification[String, AnyRef]) {
        if (notification.getCause != RemovalCause.REPLACED) {
            cacheListener.onRemoval(notification.getKey, notification.getValue, notification.getCause.toString)
        }
    }
}


