package com.echoed.util

import com.codahale.jerkson.ScalaModule
import org.codehaus.jackson.map.{DeserializationConfig, ObjectMapper}


class ScalaObjectMapper extends ObjectMapper {
    registerModule(new ScalaModule(Thread.currentThread().getContextClassLoader))
    setDeserializationConfig(getDeserializationConfig
            .without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES)
            .`with`(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .without(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES))
}
