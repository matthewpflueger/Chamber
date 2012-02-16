package com.echoed.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import org.slf4j.LoggerFactory
import org.apache.commons.codec.binary.Base64
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import com.echoed.util.Encrypter


@RunWith(classOf[JUnitRunner])
class EncrypterTest extends Spec with GivenWhenThen with ShouldMatchers {

    private val logger = LoggerFactory.getLogger(classOf[EncrypterTest])

    describe("Encrypter") {

        it("should encrypt/decrypt with a generated secret key") {
            given("a properly initialized Encrypter")
            when("asked to generate a secret and then encrypt/decrypt")
            then("it should do so without errors")

            val encrypter = new Encrypter()
            encrypter.init()

            val text = "The quick brown fox jumped over the lazy dog"

            val secret = encrypter.generateSecretKey
            val cipherText = encrypter.encrypt(text, secret)
            text should equal(encrypter.decrypt(cipherText, secret))
        }

    }
}
