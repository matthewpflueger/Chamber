package com.echoed.chamber.domain

import reflect.BeanProperty
import java.util.Date
import org.apache.commons.codec.binary.Base64
import collection.mutable.ArrayBuilder
import java.net.URLEncoder

case class EchoPossibility(
        @BeanProperty var retailerId: String,
        @BeanProperty var customerId: String,
        @BeanProperty var productId: String,
        @BeanProperty var boughtOn: Date, //Date = new Date,
        @BeanProperty var step: String,
        @BeanProperty var echoedUserId: String) {

    def this() = {
        this(null, null, null, null, null, null)
    }

    private val encoding: String = "UTF-8"

    private var _id: Option[String] = None

    def getId = id
    def setId(id: String) = id_=(id)

    def id = {
        _id = for {
            r <- Option(retailerId)
            c <- Option(customerId)
            p <- Option(productId)
            b <- Option(boughtOn)
        } yield {
            val arrayBuilder = ArrayBuilder.make[Byte]
            arrayBuilder ++= r.getBytes(encoding)
            arrayBuilder ++= c.getBytes(encoding)
            arrayBuilder ++= p.getBytes(encoding)
            arrayBuilder ++= b.toString.getBytes(encoding)
            Base64.encodeBase64URLSafeString(arrayBuilder.result())
        }
        _id.orNull
    }

    def id_=(id: String) {
        _id = Option(id)
    }

    def generateUrlParameters: String = {
        val params: Option[String] = for {
            r <- Option(retailerId)
            c <- Option(customerId)
            p <- Option(productId)
            b <- Option(boughtOn)
        } yield {
            new StringBuilder("?retailerId=")
                    .append(r)
                    .append("&customerId=")
                    .append(c)
                    .append("&productId=")
                    .append(p)
                    .append("&boughtOn=")
                    .append(URLEncoder.encode(b.toString, "UTF-8"))
                    .toString
        }
        params.getOrElse("")
    }
}
