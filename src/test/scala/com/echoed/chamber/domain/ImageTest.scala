package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.Date


@RunWith(classOf[JUnitRunner])
class ImageTest extends Spec with GivenWhenThen with ShouldMatchers {

    describe("An Image") {

        it("should return a file name and extension") {
            given("an image url")
            when("the url has characters like / and ?")
            then("still return the file name and ext")

            var image = new Image("http://somewhere.com/images/test.jpg?thisisatest")
            image.fileName should equal("test.jpg")
            image.ext should equal("jpg")

            image = new Image("test.jpg?")
            image.fileName should equal("test.jpg")
            image.ext should equal("jpg")

            image = new Image("test.jpg")
            image.fileName should equal("test.jpg")
            image.ext should equal("jpg")

            image = new Image("test")
            image.fileName should equal("test")
            image.ext should equal("")

            image = new Image("/t?")
            image.fileName should equal("t")
            image.ext should equal("")
        }

        it("should return correct isValidFormat") {
            given("an image url")
            when("it has a format on the end")
            then("return whether or not it is recognized")

            var image = new Image("/test.JPEg?")
            image.isValidFormat should be(true)

            image = new Image("test.jpG?")
            image.isValidFormat should be(true)

            image = new Image("test.pNg?")
            image.isValidFormat should be(true)

            image = new Image("test.gIf")
            image.isValidFormat should be(true)

            image = new Image("test")
            image.isValidFormat should be(false)

            image = new Image("/t?")
            image.isValidFormat should be(false)

            image = new Image("/t.test?")
            image.isValidFormat should be(false)
        }

        it("should return correct isProcessed") {
            given("an image url")
            when("it has been processed")
            then("return whether or not it is processed and what the preferredUrl is")

            var image = new Image("/test.JPEg?")
            image.isProcessed should be(false)
            image.hasOriginal should be(false)
            image.hasSized should be(false)
            image.hasThumbnail should be(false)
            image.preferredUrl should equal(image.url)
            image.preferredWidth should equal(0)
            image.preferredHeight should equal(0)


            image = image.copy(originalUrl = "test")
            image.isProcessed should be(false)
            image.hasOriginal should be(false)

            image = image.copy(originalWidth = 10)
            image.isProcessed should be(false)
            image.hasOriginal should be(false)

            image = image.copy(originalHeight = 10)
            image.isProcessed should be(false)
            image.hasOriginal should be(true)
            image.preferredUrl should equal(image.originalUrl)
            image.preferredWidth should equal(image.originalWidth)
            image.preferredHeight should equal(image.originalHeight)

            image = image.copy(sizedUrl = "sized", sizedWidth = 20, sizedHeight = 20)
            image.isProcessed should be(false)
            image.hasOriginal should be(true)
            image.hasSized should be(true)
            image.preferredUrl should equal(image.sizedUrl)
            image.preferredWidth should equal(image.sizedWidth)
            image.preferredHeight should equal(image.sizedHeight)

            image = image.copy(thumbnailUrl = "thumbnail", thumbnailWidth = 30, thumbnailHeight = 30)
            image.isProcessed should be(false)
            image.hasOriginal should be(true)
            image.hasSized should be(true)
            image.hasThumbnail should be(true)
            image.preferredUrl should equal(image.sizedUrl)
            image.preferredWidth should equal(image.sizedWidth)
            image.preferredHeight should equal(image.sizedHeight)

            image = image.copy(processedOn = new Date)
            image.isProcessed should be(true)
        }
    }
}
