package com.echoed.cache

trait CacheListener {

    def onRemoval(key: String, value: AnyRef, cause: String): Unit

}
