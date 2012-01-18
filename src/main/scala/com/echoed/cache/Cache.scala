package com.echoed.cache

import scala.collection.mutable.ConcurrentMap

trait Cache extends ConcurrentMap[String, AnyRef]/* {

    def get(key: String): Option[AnyRef]

    def getOrElse(key: String, loader: => AnyRef): AnyRef

    def getOrElseUpdate(key: String, loader: => AnyRef): AnyRef

    def remove(key: String): Option[AnyRef]

    def put(key: String, value: AnyRef): Option[AnyRef]

    def putIfAbsent(key: String, value: AnyRef): Option[AnyRef]

} */
