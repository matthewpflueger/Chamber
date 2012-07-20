package com.echoed.util

object UUID {
    def apply() = java.util.UUID.randomUUID().toString
}
