package com.echoed.util

import java.net.URLEncoder

trait AsMap {
    def asMap = ObjectUtils.asMap(this)
}

object ObjectUtils {

    def asUrlParams(o: AnyRef, prefix: String = "", encode: Boolean = false) = {
        val params = (List[String]() /: asMap(o)) {(list, keyValue) =>
            val (key, value) = keyValue
            (Option(value), encode) match {
                case (Some(v), false) => (key + "=" + v.toString) :: list
                case (Some(v), true) => (key + "=" + URLEncoder.encode(v.toString, "UTF-8")) :: list
                case _ => list
            }
        }
        prefix + params.mkString("&")
    }

    def asMap(o: AnyRef) = {
        val som = new ScalaObjectMapper()
        som.readValue(som.writeValueAsString(o), classOf[Map[String, AnyRef]])
    }

}
