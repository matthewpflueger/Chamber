package com.echoed.cache

import java.util.concurrent.{ConcurrentMap => JConcurrentMap}
import scala.collection.JavaConversions._
import scala.collection.mutable.ConcurrentMap

class LocalCache(
        cacheName: String,
        wrapped: JConcurrentMap[String, AnyRef]) { // extends Cache {

//    val cache: ConcurrentMap[String, V] = asScalaConcurrentMap[String, V](wrapped)
//
//    def get(key: String) = cache.get(key)
//
//    def getOrElse[W >: V](key: String, loader: => W) = cache.getOrElse(key, loader)
//
//    def getOrElseUpdate(key: String, loader: => V) = cache.getOrElseUpdate(key, loader)
//
//    def remove(key: String) = cache.remove(key)
//
//    def put(key: String, value: V) = cache.put(key, value)
//
//    def putIfAbsent(key: String, value: V) = cache.putIfAbsent(key, value)


    val cache = asScalaConcurrentMap[String, AnyRef](wrapped)

    def get(key: String) = cache.get(key)

    def getOrElse(key: String, loader: => AnyRef) = cache.getOrElse(key, loader)

    def getOrElseUpdate(key: String, loader: => AnyRef) = cache.getOrElseUpdate(key, loader)

    def remove(key: String) = cache.remove(key)

    def put(key: String, value: AnyRef) = cache.put(key, value)

    def putIfAbsent(key: String, value: AnyRef) = cache.putIfAbsent(key, value)
}

