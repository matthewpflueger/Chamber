package com.echoed.cache

import scala.collection.mutable.ConcurrentMap

trait CacheManager {

    def getCache[V <: AnyRef](
            cacheName: String,
            cacheListener: Option[CacheListener] = None): ConcurrentMap[String, V]

}
