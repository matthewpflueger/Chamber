package com.echoed.util

import org.codehaus.jackson.map.DeserializationConfig

object ScalaJson extends ScalaJson

trait ScalaJson extends com.codahale.jerkson.Json {
    mapper.setDeserializationConfig(mapper.getDeserializationConfig
            .without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES)
            .`with`(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .without(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES))
}
