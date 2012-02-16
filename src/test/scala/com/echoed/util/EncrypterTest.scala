package com.echoed.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import org.slf4j.LoggerFactory
import org.apache.commons.codec.binary.{Hex, Base64}


@RunWith(classOf[JUnitRunner])
class EncrypterTest extends Spec with GivenWhenThen with ShouldMatchers {

    private val logger = LoggerFactory.getLogger(classOf[EncrypterTest])

    val text = "The quick brown fox jumped over the lazy dog"

    val encrypter = new Encrypter()
    encrypter.init()

    var secret: String = null
    var cipherText: String = null

    describe("Encrypter") {

        it("should generate a generated secret key") {
            given("a properly initialized Encrypter")
            when("asked to generate a secret")
            then("it should return a generated unique secret key")

            secret = encrypter.generateSecretKey
            secret should not be(null)
        }

        it("should encrypt with a secret key") {
            given("a properly initialized Encrypter")
            when("asked to encrypt text")
            then("it should return an encrypted string")

            secret should not be(null)
            cipherText = encrypter.encrypt(text, secret)
            cipherText should not be(null)
            text should equal(encrypter.decrypt(cipherText, secret))
        }

        it("should decrypt with a secret key") {
            given("a properly initialized Encrypter")
            when("asked to decrypt text")
            then("it should return a decrypted string")

            secret should not be(null)
            cipherText should not be(null)
            text should equal(encrypter.decrypt(cipherText, secret))
        }

        it("should match php AES encryption") {
            given("a properly initialized Encrypter")
            when("asked to encrypt/decrypt some text")
            then("it should match typical php AES encryption of the same string")

            val s = Base64.encodeBase64URLSafeString("1234567890123456".getBytes("US-ASCII"))
            val cT = encrypter.encrypt(text, s)

            val hCT = Hex.encodeHexString(Base64.decodeBase64(cT))
            //see http://www.chilkatsoft.com/p/php_aes.asp
            hCT should equal("f78176ae8dfe84578529208d30f446bbb29a64dc388b5c0b63140a4f316b3f341fe7d3b1a3cc5113c81ef8dd714a1c99")
        }
    }
}
