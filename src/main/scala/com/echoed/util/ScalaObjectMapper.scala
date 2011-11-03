package com.echoed.util

import org.codehaus.jackson.map.ObjectMapper
import com.codahale.jerkson.ScalaModule


class ScalaObjectMapper extends ObjectMapper {
    registerModule(new ScalaModule(Thread.currentThread().getContextClassLoader))
}