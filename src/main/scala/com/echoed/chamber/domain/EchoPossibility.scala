package com.echoed.chamber.domain

import java.util.Date
import java.net.URLEncoder
import scala.collection.mutable.ArrayBuilder
import org.apache.commons.codec.binary.Base64
import scala.Option

case class EchoPossibility(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        retailerId: String,
        customerId: String,
        productId: String,
        boughtOn: Date,
        step: String,
        orderId: String,
        price: Float,
        imageUrl: String,
        echoedUserId: String,
        echoId: String,
        landingPageUrl: String,
        productName: String,
        category: String,
        brand: String,
        description: String,
        echoClickId: String) {

    def this(
            retailerId: String,
            customerId: String,
            productId: String,
            boughtOn: Date,
            step: String,
            orderId: String,
            price: Float,
            imageUrl: String,
            echoedUserId: String,
            echoId: String,
            landingPageUrl: String,
            productName: String,
            category: String,
            brand: String,
            description: String,
            echoClickId: String) = this(
        //NOTE: do not include any changing attributes in the hash calc.  For example, step should never
        //be included as it changes with every step the user takes to echo a purchase (button, login, etc)
        (for {
            e <- Option("UTF-8")
            r <- Option(retailerId)
            c <- Option(customerId)
            p <- Option(productId)
            b <- Option(boughtOn)
            o <- Option(orderId)
        } yield {
            val arrayBuilder = ArrayBuilder.make[Byte]
            arrayBuilder ++= r.getBytes(e)
            arrayBuilder ++= c.getBytes(e)
            arrayBuilder ++= p.getBytes(e)
            arrayBuilder ++= b.toString.getBytes(e)
            arrayBuilder ++= o.getBytes(e)
            Base64.encodeBase64URLSafeString(arrayBuilder.result())
        }).orNull,
        new Date,
        new Date,
        retailerId,
        customerId,
        productId,
        boughtOn,
        step,
        orderId,
        price,
        imageUrl,
        echoedUserId,
        echoId,
        landingPageUrl,
        productName,
        category,
        brand,
        description,
        echoClickId)


    def asUrlParams(prefix: String = "", encode: Boolean = false) = {
        val params = (List[String]() /: asMap) {(list, keyValue) =>
            val (key, value) = keyValue
            (Option(value), encode) match {
                case (Some(v), false) => (key + "=" + v.toString) :: list
                case (Some(v), true) => (key + "=" + URLEncoder.encode(v.toString, "UTF-8")) :: list
                case _ => list
            }
        }
        prefix + params.mkString("&")
    }

    def asMap = {
        (Map[String, String]() /: this.getClass.getDeclaredFields) {(a, f) =>
            f.setAccessible(true)
            Option(f.get(this)) match {
                case Some(v) => a + (f.getName -> v.toString)
                case _ => a
            }
        }
    }
}

