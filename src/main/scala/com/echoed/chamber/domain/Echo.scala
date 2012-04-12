package com.echoed.chamber.domain

import java.util.{UUID, Date}
import java.lang.{Math => JMath}
import collection.mutable.ArrayBuilder
import org.apache.commons.codec.binary.Base64
import java.net.URLEncoder


case class Echo(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        retailerId: String,
        echoedUserId: String,
        facebookPostId: String,
        twitterStatusId: String,
        @deprecated echoPossibilityId: String,
        retailerSettingsId: String,
        echoMetricsId: String,
        echoClickId: String,
        step: String,
        browserId: String,
        ipAddress: String,
        userAgent: String,
        referrerUrl: String,
        view: String,
        order: Order,
        product: Product,
        image: Image) {


    this.ensuring(step != null, "Step cannot be null")

    val isEchoed = echoedUserId != null

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

    val productId = product.productId
    val productName = product.productName
    val price = product.price
    val landingPageUrl = product.landingPageUrl
    val brand = product.brand

    val orderId = order.orderId
    val customerId = order.customerId
    val boughtOn = order.boughtOn

    val imageUrl = image.preferredUrl
    val imageWidth = image.preferredWidth
    val imageHeight = image.preferredHeight
}


object Echo {

    def make(
            retailerId: String,
            customerId: String,
            productId: String,
            boughtOn: Date,
            step: String,
            orderId: String,
            price: Float,
            imageUrl: String,
            landingPageUrl: String,
            productName: String,
            category: String,
            brand: String,
            description: String,
            echoClickId:String,
            browserId: String,
            ipAddress: String,
            userAgent: String,
            referrerUrl: String,
            partnerSettingsId: String = null,
            view: String = null) = {

        val id = UUID.randomUUID.toString
        val date = new Date

        //NOTE: do not include any changing attributes in the hash calc.  For example, step should never
        //be included as it changes with every step the user takes to echo a purchase (button, login, etc)
        val echoPossibilityId = (for {
                e <- Option("UTF-8")
                r <- Option(retailerId)
                c <- Option(customerId)
                p <- Option(productId)
                o <- Option(orderId)
            } yield {
                val arrayBuilder = ArrayBuilder.make[Byte]
                arrayBuilder ++= r.getBytes(e)
                arrayBuilder ++= c.getBytes(e)
                arrayBuilder ++= p.getBytes(e)
                arrayBuilder ++= o.getBytes(e)
                Base64.encodeBase64URLSafeString(arrayBuilder.result())
            }).orNull

        Echo(
            id = id,
            updatedOn = date,
            createdOn = date,
            retailerId = retailerId,
            echoedUserId = null,
            facebookPostId = null,
            twitterStatusId = null,
            echoPossibilityId = echoPossibilityId,
            retailerSettingsId = partnerSettingsId,
            echoMetricsId = null,
            echoClickId = echoClickId,
            step = step,
            browserId = browserId,
            ipAddress = ipAddress,
            userAgent = Option(userAgent).map(_.take(254)).orNull,
            referrerUrl = referrerUrl,
            view = view,
            order = Order(id, date, date, customerId, boughtOn, orderId),
            product = Product(
                        id,
                        date,
                        date,
                        productId,
                        price,
                        landingPageUrl,
                        productName,
                        category,
                        brand,
                        Option(description).map(_.take(1023)).orNull),
            image = new Image(imageUrl))
    }
}



