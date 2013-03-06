package com.echoed.util

import java.util.Properties
import org.apache.commons.codec.digest.DigestUtils

case class CloudinaryUtil(cloudinaryProperties: Properties) {

    val endpoint = cloudinaryProperties.getProperty("endpoint")
    val apiKey = cloudinaryProperties.getProperty("apiKey")
    val name = cloudinaryProperties.getProperty("name")

    def sign(params: Map[String, String]) =
        DigestUtils.shaHex(
            params
                .toList
                .sorted
                .map { case (k, v) => "%s=%s" format(k, v) }
                .mkString("&") + cloudinaryProperties.getProperty("secret"))
}
