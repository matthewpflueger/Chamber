package com.echoed.cache

import scala.collection.JavaConversions._
import com.google.common.collect.MapMaker
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import scala.collection.mutable.ConcurrentMap
import scala.reflect.BeanProperty
import com.google.common.cache.{RemovalCause, RemovalNotification, RemovalListener, CacheBuilder}

class LocalCacheManager extends CacheManager {

    private val logger = LoggerFactory.getLogger(classOf[LocalCacheManager])

    @BeanProperty var expireInMinutes = 30

    private val caches: ConcurrentMap[String, ConcurrentMap[String, AnyRef]] =
                new MapMaker().concurrencyLevel(4).makeMap[String, ConcurrentMap[String, AnyRef]]()

    def getCache[V <: AnyRef](cacheName: String, cacheListener: Option[CacheListener] = None) = {
        caches.getOrElseUpdate(cacheName, {
            CacheBuilder.newBuilder()
                        .concurrencyLevel(4)
                        .softValues
                        .expireAfterAccess(expireInMinutes, TimeUnit.MINUTES)
                        .removalListener(new RemovalListenerProxy(cacheListener.getOrElse(NoOpCacheListener)))
                        .build[String, AnyRef]
                        .asMap
        }).asInstanceOf[ConcurrentMap[String, V]]
    }
}


object NoOpCacheListener extends CacheListener {
    def onRemoval(key: String, value: AnyRef, cause: String) {
        //no op
    }
}

class RemovalListenerProxy(cacheListener: CacheListener) extends RemovalListener[String, AnyRef] {
    private val logger = LoggerFactory.getLogger(classOf[RemovalListenerProxy])

    def onRemoval(notification: RemovalNotification[String, AnyRef]) {
        if (notification.getCause != RemovalCause.REPLACED) {
            cacheListener.onRemoval(notification.getKey, notification.getValue, notification.getCause.toString)
        }
    }
}


