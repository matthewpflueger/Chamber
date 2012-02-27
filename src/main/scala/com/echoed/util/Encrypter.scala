package com.echoed.util

import org.apache.commons.codec.binary.Base64
import javax.crypto.{Cipher, KeyGenerator}
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import scala.reflect.BeanProperty

class Encrypter {

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

    def encrypt(text: String, secret: String = secret) = {
        var textBytes = text.getBytes("UTF-8")
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


    def decrypt(text: String, secret: String = secret) = {
        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(
            Cipher.DECRYPT_MODE,
            new SecretKeySpec(Base64.decodeBase64(secret), algorithm),
            ivParameterSpec)

        val decrypted = cipher.doFinal(Base64.decodeBase64(text))
        new String(decrypted, "UTF-8").trim()
    }
}

object Encrypter extends App {
    val encrypter = new Encrypter
    encrypter.init()
    Console.println("Secret key: %s" format encrypter.generateSecretKey)
}


