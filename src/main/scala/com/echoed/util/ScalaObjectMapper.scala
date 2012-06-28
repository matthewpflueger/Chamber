package com.echoed.util


import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}


object ScalaObjectMapper {
    def apply[T](
            value: String,
            valueType: Class[T],
            unwrap: Boolean = false) = new ScalaObjectMapper(unwrap).readValue(value, valueType)
}

class ScalaObjectMapper(unwrap: Boolean = false) extends ObjectMapper {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
    configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    configure(DeserializationFeature.UNWRAP_ROOT_VALUE, unwrap)


    registerModule(DefaultScalaModule)

    def this() = this(false)
}
