package com.echoed.chamber.services.image

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import com.echoed.chamber.dao._
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import akka.testkit.TestActorRef
import akka.util.Duration
import com.echoed.util.{BlobStore, IntegrationTest}
import com.echoed.chamber.domain.Image
import akka.dispatch.{AlreadyCompletedFuture, DefaultCompletableFuture}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:apiIT.xml"))
class ImageServiceActorIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var imageDao: ImageDao = _
    @Autowired @BeanProperty var blobStore: BlobStore = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val imageServiceActor = TestActorRef[ImageServiceActor]

    val images = dataCreator.images
    val noImage = dataCreator.noImage
    val smallImage = dataCreator.smallImage
    val invalidImage = new Image("someimagewithinvalidformat")

    var image: Image = _

    def cleanup() {
        images.foreach { i =>
            imageDao.deleteByUrl(i.url)
        }
        imageDao.deleteByUrl(noImage.url)
        imageDao.deleteByUrl(smallImage.url)
        imageDao.deleteByUrl(invalidImage.url)
    }

    override def beforeAll = {
        cleanup
        imageDao.insert(images(0))
        imageDao.insert(noImage)
        imageDao.insert(smallImage)
        imageDao.insert(invalidImage)

        imageServiceActor.underlyingActor.imageDao = imageDao
        imageServiceActor.underlyingActor.blobStore = blobStore
        imageServiceActor.underlyingActor.lastProcessedWithinMinutes = -1 //turn off finding unprocessed images...

        imageServiceActor.start

        imageServiceActor.isRunning should be(true)
    }

    override def afterAll = cleanup


    def existsAndDelete(fileName: String) {
        blobStore
            .exists(fileName)
            .flatMap { b =>
                if (b) blobStore.delete(fileName)
                else new AlreadyCompletedFuture[Boolean](Left(new RuntimeException("%s not found" format fileName)))}
            .await(Duration(30, "seconds"))
    }

    feature("Images will be downloaded and processed") {

        info("As a developer")
        info("I want to know process our partner's product images")
        info("So that I can verify that they are good")

        scenario("a valid product image gets processed", IntegrationTest) {
            given("a partner product image")
            when("it is valid and not already processed")
            then("process the image")
            and("save the image data in the database")

            val future = new DefaultCompletableFuture[ProcessImageResponse]()
            imageServiceActor.underlyingActor.future = Some(future)

            imageServiceActor ! ProcessImage(images(0))
            image = future.await(Duration(30, "seconds")).get.resultOrException

            image.id should equal(images(0).id)
            image.isProcessed should equal(imageDao.findById(images(0).id).isProcessed)

            existsAndDelete(image.originalFileName.get)
            existsAndDelete(image.sizedFileName.get)
            existsAndDelete(image.thumbnailFileName.get)
        }

        scenario("an already processed image does not get processed again", IntegrationTest) {
            image should not be(null)

            given("an image")
            when("it has been processed")
            then("do not process the message")

            val future = new DefaultCompletableFuture[ProcessImageResponse]()
            imageServiceActor.underlyingActor.future = Some(future)

            imageServiceActor ! ProcessImage(image)

            val img = future.await(Duration(1, "seconds")).get.resultOrException

            img.id should equal(image.id)
            img.sizedUrl should equal(image.sizedUrl)
            img.thumbnailUrl should equal(image.thumbnailUrl)
        }

        scenario("a not found product image does not get processed", IntegrationTest) {
            given("a partner product image")
            when("it is not found")
            then("do not process the image")
            and("and save the result in the database")

            val future = new DefaultCompletableFuture[ProcessImageResponse]()
            imageServiceActor.underlyingActor.future = Some(future)

            imageServiceActor ! ProcessImage(noImage)
            evaluating { future.await(Duration(30, "seconds")).get.resultOrException } should produce [ImageNotFound]

            val img = imageDao.findById(noImage.id)
            img should not be(null)
            img.isProcessed should be(false)
            img.preferredUrl should equal(img.url)
            img.processedOn should not be(null)
            img.processedStatus contains "Image not found"
        }

        scenario("an invalid width image does not get processed", IntegrationTest) {
            given("a partner product image")
            when("it is too small")
            then("do not process the image")
            and("and save the result in the database")

            val future = new DefaultCompletableFuture[ProcessImageResponse]()
            imageServiceActor.underlyingActor.future = Some(future)

            imageServiceActor ! ProcessImage(smallImage)
            evaluating { future.await(Duration(30, "seconds")).get.resultOrException } should produce [InvalidImageWidth]

            val img = imageDao.findById(noImage.id)
            img should not be(null)
            img.isProcessed should be(false)
            img.preferredUrl should equal(img.url)
            img.processedOn should not be(null)
            img.processedStatus contains "Invalid image width"
        }

        scenario("an invalid format image does not get processed", IntegrationTest) {
            given("a partner product image")
            when("it has an unrecognized image format")
            then("do not process the image")
            and("and save the result in the database")

            val future = new DefaultCompletableFuture[ProcessImageResponse]()
            imageServiceActor.underlyingActor.future = Some(future)

            imageServiceActor ! ProcessImage(invalidImage)
            evaluating { future.await(Duration(30, "seconds")).get.resultOrException } should produce [InvalidImageFormat]

            val img = imageDao.findById(noImage.id)
            img should not be(null)
            img.isProcessed should be(false)
            img.preferredUrl should equal(img.url)
            img.processedOn should not be(null)
            img.processedStatus contains "Invalid image format"
        }

    }
}
