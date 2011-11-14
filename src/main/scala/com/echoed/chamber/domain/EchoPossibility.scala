package com.echoed.chamber.domain

import reflect.BeanProperty
import java.util.Date
import org.apache.commons.codec.binary.Base64
import collection.mutable.ArrayBuilder
import java.net.URLEncoder

case class EchoPossibility(
                      var _id: String,
        @BeanProperty var retailerId: String = null,
        @BeanProperty var customerId: String = null,
        @BeanProperty var productId: String = null,
        @BeanProperty var boughtOn: Date = null,
        @BeanProperty var step: String = null,
        @BeanProperty var orderId: String = null,
        @BeanProperty var price: String = null,
        @BeanProperty var imageUrl: String = null,
        @BeanProperty var echoedUserId: String = null,
        @BeanProperty var echoId: String = null) {

    def this() = {
        this(null)
    }

    private val encoding: String = "UTF-8"

    def getId = id

    def id = {
        //NOTE: do not include any changing attributes in the hash calc.  For example, step should never
        //be included as it changes with every step the user takes to echo a purchase (button, login, etc)
        val optionalId: Option[String] = for {
            r <- Option(retailerId)
            c <- Option(customerId)
            p <- Option(productId)
            b <- Option(boughtOn)
            o <- Option(orderId)
            e <- Option(price)
            i <- Option(imageUrl)
        } yield {
            val arrayBuilder = ArrayBuilder.make[Byte]
            arrayBuilder ++= r.getBytes(encoding)
            arrayBuilder ++= c.getBytes(encoding)
            arrayBuilder ++= p.getBytes(encoding)
            arrayBuilder ++= b.toString.getBytes(encoding)
            arrayBuilder ++= o.getBytes(encoding)
            arrayBuilder ++= e.getBytes(encoding)
            arrayBuilder ++= i.getBytes(encoding)
            Base64.encodeBase64URLSafeString(arrayBuilder.result())
        }
        optionalId.orNull
    }

    def generateUrlParameters: String = {
        val params: Option[String] = for {
            r <- Option(retailerId)
            c <- Option(customerId)
            p <- Option(productId)
            b <- Option(boughtOn)
            o <- Option(orderId)
            e <- Option(price)
            i <- Option(imageUrl)
        } yield {
            new StringBuilder("?retailerId=")
                    .append(r)
                    .append("&customerId=")
                    .append(c)
                    .append("&productId=")
                    .append(p)
                    .append("&boughtOn=")
                    .append(URLEncoder.encode(b.toString, "UTF-8"))
                    .append("&orderId=")
                    .append(o)
                    .append("&price=")
                    .append(e)
                    .append("&imageUrl=")
                    .append(URLEncoder.encode(i, "UTF-8"))
                    .toString
        }
        params.getOrElse("")
    }
}
