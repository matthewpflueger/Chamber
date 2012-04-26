package com.echoed.util

import java.net.URLEncoder

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
        (Map[String, String]() /: o.getClass.getDeclaredFields) {(a, f) =>
            f.setAccessible(true)
            Option(f.get(this)) match {
                case Some(v) => a + (f.getName -> v.toString)
                case _ => a
            }
        }
    }

}
