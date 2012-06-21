package com.echoed.util

import org.apache.commons.codec.binary.Base64
import javax.crypto.{Cipher, KeyGenerator}
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import scala.reflect.BeanProperty
import java.io._
import scala.io.Source
import org.slf4j.LoggerFactory
import java.util.zip._

class Encrypter {

    private val logger = LoggerFactory.getLogger(classOf[Encrypter])

    @BeanProperty var keyGenerator: KeyGenerator = _
    @BeanProperty var ivParameterSpec: IvParameterSpec = _

    @BeanProperty var secret: String = _ //"PHHabG5MSw6hv4lZJg-Ppg"

    @BeanProperty var algorithm: String = "AES"
    @BeanProperty var cipherTransformation: String = "AES/CBC/NoPadding"
    @BeanProperty var iv: String = "1234567890123456"

    def init() {
        if (keyGenerator == null) {
            keyGenerator = KeyGenerator.getInstance(algorithm)
            keyGenerator.init(128)
        }
        if (ivParameterSpec == null) {
            ivParameterSpec = new IvParameterSpec(iv.getBytes("UTF-8"))
        }
    }

    def generateSecretKey = {
        synchronized {
            Base64.encodeBase64URLSafeString(keyGenerator.generateKey.getEncoded)
        }
    }

    def encrypt(text: String, secret: String = secret, gzip: Boolean = true) = {
        var textBytes = text.getBytes("UTF-8")
        if (gzip) {
            val os = new ByteArrayOutputStream(textBytes.length)
            val gzip = new GZIPOutputStream(os)
            gzip.write(textBytes)
            gzip.flush()
            gzip.close()
            textBytes = os.toByteArray
        }

        if (textBytes.length % 16 != 0) {
            val newBytes = new Array[Byte](textBytes.length + 16 - (textBytes.length % 16))
            System.arraycopy(textBytes, 0, newBytes, 0, textBytes.length)
            textBytes = newBytes
        }

        val encCipher = Cipher.getInstance(cipherTransformation)
        encCipher.init(
            Cipher.ENCRYPT_MODE,
            new SecretKeySpec(Base64.decodeBase64(secret), algorithm),
            ivParameterSpec)

        Base64.encodeBase64URLSafeString(encCipher.doFinal(textBytes))
    }


    def decrypt(text: String, secret: String = secret, gunzip: Boolean = true) = {
        logger.debug("Decrypting {} with secret {}", text, secret)
        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(
            Cipher.DECRYPT_MODE,
            new SecretKeySpec(Base64.decodeBase64(secret), algorithm),
            ivParameterSpec)

        val decrypted = cipher.doFinal(Base64.decodeBase64(text))
        if (gunzip) {
            try{
                new String(Source.fromInputStream(new GZIPInputStream(new ByteArrayInputStream(decrypted)), "UTF-8").toArray)
            }
            catch {
                case e: IOException =>
                    new String(Source.fromInputStream(new InflaterInputStream(new ByteArrayInputStream(Base64.decodeBase64(decrypted)), new Inflater(true)), "UTF-8").toArray)
            }
        } else new String(decrypted, "UTF-8").trim()
    }
}

object Encrypter extends App {
    val encrypter = new Encrypter
    encrypter.init()
    println(encrypter.generateSecretKey)
//    scala.Console.println("Secret key: %s" format encrypter.generateSecretKey)
//    val secret = "135ikqAU6QYGE2b1A3kRUg"
//    val data = """{"customerId":"1","boughtOn":1330458341,"orderId":82,"items":[{"productId":"50","productName":"Clear brief BRELLI","price":46,"imageUrl":"http:\/\/localhost\/image\/data\/a clear brief.jpg","landingPageUrl":"http:\/\/localhost\/brief-brelli","category":"Accessories","brand":"BRELLI","description":"<p>\r\n\tThe original BRELLI is the perfect rain umbrella!&nbsp; It is designed to withstand wind gusts up to 40 MPH, yet it is incredibly lightweight and easy to carry. The briefBRELLI is compact sized for that emergency rain.&nbsp; The BRELLI is manufactured using sustainable and renewable bamboo, organic cotton, and our own biodegradable plastic.&nbsp; Every BRELLI comes with its own custom cotton carrying case for easy handling and storage.<\/p>\r\n<p>\r\n\t&nbsp;<\/p>\r\n<p>\r\n\tProduct Details:<\/p>\r\n<ul style="padding-right: 40px; ">\r\n\t<li>\r\n\t\t100% biodegradable<\/li>\r\n\t<li>\r\n\t\tClear canopy<\/li>\r\n\t<li>\r\n\t\tDiameter: 28\u201d<\/li>\r\n\t<li>\r\n\t\tLength: 14\u201d<\/li>\r\n\t<li>\r\n\t\tHandmade in Thailand<\/li>\r\n<\/ul>\r\n"},{"productId":"52","productName":"Clear BRELLI - x-small","price":48,"imageUrl":"http:\/\/localhost\/image\/data\/b clear x small.jpg","landingPageUrl":"http:\/\/localhost\/index.php?route=product\/product&product_id=52","category":"Accessories","brand":"BRELLI","description":"<p>\r\n\tThe original BRELLI is the perfect rain umbrella!&nbsp; It is designed to withstand wind gusts up to 40 MPH, yet it is incredibly lightweight and easy to carry. The BRELLI is manufactured using sustainable and renewable bamboo, organic cotton, and our own biodegradable plastic.&nbsp; Every BRELLI comes with its own custom cotton carrying case for easy handling and storage.<\/p>\r\n<p>\r\n\t&nbsp;<\/p>\r\n<p>\r\n\tProduct Details:<\/p>\r\n<ul>\r\n\t<li>\r\n\t\t100% biodegradable<\/li>\r\n\t<li>\r\n\t\tClear canopy<\/li>\r\n\t<li>\r\n\t\tDiameter: 35\u201d<\/li>\r\n\t<li>\r\n\t\tLength: 24\u201d<\/li>\r\n\t<li>\r\n\t\tWeight: 10 ounces<\/li>\r\n\t<li>\r\n\t\tHandmade in Thailand<\/li>\r\n<\/ul>\r\n"},{"productId":"67","productName":"bbprint BRELLI - small","price":57,"imageUrl":"http:\/\/localhost\/image\/data\/print_small.jpg","landingPageUrl":"http:\/\/localhost\/index.php?route=product\/product&product_id=67","category":"Accessories","brand":"BRELLI","description":"<p>\r\n\tThe bbprintBRELLI is the perfect designer umbrellla...rain 'r shine! It provides 99% UVA\/UVB\/UVC sun protection. It is designed to withstand wind gusts up to 40 MPH, yet it is incredibly lightweight and easy to carry. The bbprintBRELLI is manufactured using sustainable and renewable bamboo, organic cotton, and our own biodegradable plastic.&nbsp; Every bbprintBRELLI comes with its own custom cotton carrying case for easy handling and storage.<\/p>\r\n<p style="font-weight: bold;">\r\n\tProduct Details:<\/p>\r\n<ul>\r\n\t<li>\r\n\t\t99% UVA\/UVB\/UVC sun protection<\/li>\r\n\t<li>\r\n\t\t100% biodegradable<\/li>\r\n\t<li>\r\n\t\tClear printed canopy<\/li>\r\n\t<li>\r\n\t\tDiameter: 37\u201d<\/li>\r\n\t<li>\r\n\t\tLength: 25.5\u201d<\/li>\r\n\t<li>\r\n\t\tHandmade in Thailand<\/li>\r\n\t<li>\r\n\t\tLimited Edition<\/li>\r\n<\/ul>\r\n"},{"productId":"68","productName":"bbprint BRELLI - medium","price":64,"imageUrl":"http:\/\/localhost\/image\/data\/print_medium.jpg","landingPageUrl":"http:\/\/localhost\/index.php?route=product\/product&product_id=68","category":"Accessories","brand":"BRELLI","description":"<p>\r\n\tThe bbprintBRELLI is the perfect designer umbrellla...rain 'r shine! It provides 99% UVA\/UVB\/UVC sun protection. It is designed to withstand wind gusts up to 40 MPH, yet it is incredibly lightweight and easy to carry. The bbprintBRELLI is manufactured using sustainable and renewable bamboo, organic cotton, and our own biodegradable plastic.&nbsp; Every bbprintBRELLI comes with its own custom cotton carrying case for easy handling and storage.&nbsp;<\/p>\r\n<p>\r\n\t&nbsp;<\/p>\r\n<p>\r\n\tProduct Details:<\/p>\r\n<ul style="padding-right: 40px; ">\r\n\t<li>\r\n\t\t99% UVA\/UVB\/UVC sun protection<\/li>\r\n\t<li>\r\n\t\t100% biodegradable<\/li>\r\n\t<li>\r\n\t\tClear printed canopy<\/li>\r\n\t<li>\r\n\t\tDiameter: 45\u201d<\/li>\r\n\t<li>\r\n\t\tLength: 29\u201d<\/li>\r\n\t<li>\r\n\t\tHas two convenient settings; high and low<\/li>\r\n\t<li>\r\n\t\tHandmade in Thailand<\/li>\r\n\t<li>\r\n\t\tLimited Edition<\/li>\r\n<\/ul>\r\n<div style="background-color: transparent; ">\r\n\t&nbsp;<\/div>\r\n<p>\r\n\t&nbsp;<\/p>\r\n"},{"productId":"64","productName":"BRELLIwear Kid's Poncho","price":22,"imageUrl":"http:\/\/localhost\/image\/data\/r kids poncho.jpg","landingPageUrl":"http:\/\/localhost\/BRELLIwear-kids-poncho","category":"Accessories","brand":"BRELLI","description":"<p>\r\n\tThe BRELLIwear kid's poncho is for your little angels when they get caught in the rain. The poncho comes with a big pocket and a hood. &nbsp;It can also be used with the BRELLImarkers to create a custom design "so their art won't wash off in the rain". As rain accessories go... the poncho is not only kid-friendly, but earth friendly... it is 100% biodegradable!&nbsp;<\/p>\r\n<p>\r\n\tProduct Details:<\/p>\r\n<ul>\r\n\t<li style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; border-top-width: 0px; border-right-width: 0px; border-bottom-width: 0px; border-left-width: 0px; border-style: initial; border-color: initial; outline-width: 0px; outline-style: initial; outline-color: initial; vertical-align: baseline; ">\r\n\t\tOne Size<\/li>\r\n\t<li style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; border-top-width: 0px; border-right-width: 0px; border-bottom-width: 0px; border-left-width: 0px; border-style: initial; border-color: initial; outline-width: 0px; outline-style: initial; outline-color: initial; vertical-align: baseline; ">\r\n\t\tFits Children 4-9<\/li>\r\n\t<li style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; border-top-width: 0px; border-right-width: 0px; border-bottom-width: 0px; border-left-width: 0px; border-style: initial; border-color: initial; outline-width: 0px; outline-style: initial; outline-color: initial; vertical-align: baseline; ">\r\n\t\tBiodegradable<\/li>\r\n\t<li style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; border-top-width: 0px; border-right-width: 0px; border-bottom-width: 0px; border-left-width: 0px; border-style: initial; border-color: initial; outline-width: 0px; outline-style: initial; outline-color: initial; vertical-align: baseline; ">\r\n\t\tFor use with BRELLImarkers<\/li>\r\n<\/ul>\r\n"}]}"""
//    val encrypted =  encrypter.encrypt(data, secret)
//    scala.Console.println("Encrypted:")
//    scala.Console.println(encrypted)
//
//
//    val decrypted = encrypter.decrypt(encrypted, secret)
//    scala.Console.println("\n\nDecrypted original:")
//    scala.Console.println(decrypted)

    //ZEBC2OLDyg6_yG2MMIbR7QXkbcOUIgmD9lsHDWMS8RQ with secret NDDaXyF7GZC9LrvNmAE6Bw
//    scala.Console.println("\n\nDecrptyed:")
//    scala.Console.println(encrypter.decrypt("ZEBC2OLDyg6_yG2MMIbR7QXkbcOUIgmD9lsHDWMS8RQ", "NDDaXyF7GZC9LrvNmAE6Bw"))
//    scala.Console.println(encrypter.decrypt(
//    """gcz5rCCoZyV2Oygi0edRGGbAeJT1l4yDowOtqarLCehZGP5PmJf71JWuBQtCbYCDl5zkZOGNrsjFcXYReWXSvRWwdKiSTtRfvrG6tyTPKrg1_F7w9E-px50uJx9o8Uqufq0gUIMkCM0d7kicAof2oYhmW2Omq5eTGiscH0TFIwY7ahrzJfgvW5ESt004Omblyb-UNOFlUTgT_vjhCfzugubF411YedY1N3RLXcEt2ejybjBMOm6CLc89yjR8WEAipbEYrd51Ng-wjENF6TSPmPTTm9thilkoJW68FGyUsaRoajrHRImdQ8EAMT6yzIA72pRpG1nqxQtwql3w6AV7AXivkwibv_hn8got4e9Y5LeikzrprrJ16T7gQ_mMT3JpXzWAc1m72JIQsmZ1uk5UmKKggGfLXZbHCFtdIn4ReTQkhTTLvdWy7bXzd_jvOYC3QEvZIXRMwKjjhqcNnH7_TWajqMIue7gGQPVI3oeYT2sjoEJakCAEN_0MRPPTZ2c8I5VoDOLxl99FQEwNO-7xN9Kn6BH_1YN2fYjsZ43sZdu4w8sxZK6bWwOj9BcGt8rXmYSNrkZFn-BWAu9honZCYlFbEpaY0bXZD0_oyb5MxHdiu-YdiL3odcSSrQOF1fbHq0e6kjnwRfgQJy4rFqAP-bvktVASMIabnpF0XXJSlSMUx2J4Vac4tKfWTayrfurJnBIrGaQqjtlv2U0eN0wHXDL4320XXy3PpZuITNwxaQ6Nn8Amq4w6iKX6ZWsRvnpcKfb9TVDSJfL6SPtVdaJz1IGcgxbztJwwFygce4zd6VJNE_a4VFUMcrU7fqs2XueX_Gkyz0WyUl59CHS-Er44mVR6-tvsfS8R-UX8HA5TGl4H6iQmSxbmFtvE5fhpO4J_x0wRrCKNG6nUYODuGUq3S28M8UW19AJnVD9wY7JKAHoxb44FVYFuyodowph7aXPoQsh-1j0dFLgCSocMUPssmDKjyisQcTWFgAQxcOvEKfkYkOrVoJ7wkPvTF_Cn39ExuSJ47XKg1YrPItO3rMkCwm-6555MU_gX0RJZpxlWoGl5uqzUyp8AT1e8iNZonM9bF_2LFi8pedkhDYtiSHh_bY-C-FHLkaZChdpn7_oyw2HaHAna9enrmJSS1LJZLHFWUND8uIfSy7Pi1pZyzhROaqnyLIJXbZ_2p2SIfdYAIC3PiaUp6HelNUIDxg8x_lmyfPmt9urEPldxolwh0omcuuD60wKq1fZBMOcViaLIKprI2n3cSWZlahn-QuP4tR_cEVtoUCa92dB18laS_x4LCjWCM8O1RNUBD6w_5rLKP1A6yGbM5mErEg9oJ1hvUawxs7ByVloGprwkkpaknBhFz43rNoP0F6gE49vqqLMsa3DZjzCjupIKqVasN00Nyjt_tPXqYF7BytaLvCoM5DtaBFVSXR8Xc9BXLiRm9NlkXE3fu66kEJEWFAxfPmD9fuP47FN5MmKqH8PuB1Zdl4PVD_KG9KGruBXXveqb05whrqK4gXuyqb5KiCE7rxdxZXegM-3bQvJvUrk2BBT6uH1XsIn-s96hxwvNwq52UAy8yZFGbHOmgy5JSKu9dAgMDB20VIZb20Spq6wNxhdUAUg-WxjEG0vOfIAi-GeC5L2JIOOZjzqFWMi0Nro3xo7mCmoPtIMT-2gUwqDR2oqFKq67hu0t3CFD4ypDIXeTYfxVAT_PX68aB-vdn0gRNIMRo4JQjiln8gNMKwCVafjbGYl4mGBMTk_Ww9eFpx91KENbmoQPaZdbFbalPV09ouOGDH9Zg6ECsdZVeH4oI3wxWHah7_br8VKtZXYAza51irmg--g""",
//    "135ikqAU6QYGE2b1A3kRUg"))
    override def main(args: Array[String]) { super.main(args) }
}


