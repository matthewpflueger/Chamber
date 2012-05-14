package com.echoed.cache

import scala.collection.mutable.ConcurrentMap

trait Cache extends ConcurrentMap[String, AnyRef]
