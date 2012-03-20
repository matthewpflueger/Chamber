package com.echoed.chamber.services.image

import org.slf4j.LoggerFactory
import com.echoed.chamber.dao._
import java.security.MessageDigest
import javax.imageio.ImageIO
import com.google.common.io.ByteStreams
import com.echoed.util.BlobStore
import org.apache.commons.codec.binary.Base64
import java.net.URL
import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import akka.actor._
import com.echoed.chamber.domain.Image
import java.io.{ByteArrayOutputStream, InputStream, ByteArrayInputStream}
import scala.collection.mutable.ListBuffer
import java.util.Date


class ImageProcessorActor(
        var image: Image,
        imageDao: ImageDao,
        blobStore: BlobStore,
        sizedImageTargetWidth: Int = 230,
        thumbImageTargetWidth: Int = 120,
        thumbImageTargetHeight: Int = 120,
        imageTargetFormat: String = "png") extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[ImageProcessorActor])

    private val imageFormats = List("jpeg", "jpg", "png", "gif")

    private var responses: Option[ListBuffer[(GrabImage, Channel[GrabImageResponse])]] = None
    private var grabException: Option[GrabImageException] = None

    private var originalImageWidth = 0
    private var originalImageHeight = 0

    private var sizedImageWidth = 0
    private var sizedImageHeight = 0


    override def preStart() {
        val me = self

        //clear out old values which will be re-populated during the processing...
        image = image.copy(imageGrabbedOn = null, imageGrabbedStatus = null, imageWidth = 0, imageHeight = 0)


        var originalInputStream: Option[InputStream] = None
        var originalBufferedImage: Option[BufferedImage] = None
        var sizedBufferedImage: Option[BufferedImage] = None
        var thumbBufferedImage: Option[BufferedImage] = None
        var thumbCroppedBufferedImage: Option[BufferedImage] = None

        var baseFileName = "to be determined..."
        val metadata = Some(Map[String, String]("original-url" -> image.imageOriginalUrl))

        try {
            logger.debug("Starting download of image {}", image.imageOriginalUrl)

            try {
                val connection = new URL(image.imageOriginalUrl).openConnection()
                connection.setConnectTimeout(5000)
                connection.setReadTimeout(5000)
                originalInputStream = Some(connection.getInputStream)


                val originalBytes: Array[Byte] = ByteStreams.toByteArray(connection.getInputStream)
                logger.debug("Successfully downloaded image {}", image.imageOriginalUrl)

                val messageDigest = MessageDigest.getInstance("MD5")
                val md5 = messageDigest.digest(originalBytes)


                baseFileName = Base64.encodeBase64URLSafeString(md5)
                val originalExt = image.imageOriginalUrl.substring(image.imageOriginalUrl.lastIndexOf('.') + 1).toLowerCase
                if (!imageFormats.contains(originalExt)) throw new ImageException("Unknown image format %s" format originalExt)
                val originalContentType = "image/%s" format originalExt

                val originalImageUploadCallback: Either[Throwable, String] => Unit = _.fold(
                    e => {
                        logger.error("Error storing original image %s with name %s" format(image.imageOriginalUrl, originalFileName), e)
                        me ! OriginalImageUploadError(e)
                    },
                    url => {
                        logger.debug("Successfully stored {} at {}", image.imageOriginalUrl, url)
                        me ! OriginalImageUploaded(url)
                    })

                logger.debug("Reading image {}", image.imageOriginalUrl)
                originalBufferedImage = Some(ImageIO.read(new ByteArrayInputStream(originalBytes)))
                originalImageWidth = originalBufferedImage.get.getWidth
                originalImageHeight = originalBufferedImage.get.getHeight
                val originalFileName = "%s-%sx%s-original.%s" format(
                        baseFileName,
                        originalImageWidth,
                        originalImageHeight,
                        originalExt)

                logger.debug("Saving original image of {} as {}", image.imageOriginalUrl, originalFileName)
                blobStore.store(
                    originalBytes,
                    originalFileName,
                    originalContentType,
                    originalImageUploadCallback,
                    metadata)
            } catch {
                case e =>
                    logger.error("Failed to process original image %s" format image.imageOriginalUrl)
                    image = image.copy(imageGrabbedStatus = e.getMessage.take(254))
                    grabException = Some(GrabImageException(image, c = e))
                    imageDao.update(image)
                    throw e
            }


            logger.debug("Resizing original image {} to target width {}", image.imageOriginalUrl, sizedImageTargetWidth)
            sizedBufferedImage = Some(Scalr.resize(
                    originalBufferedImage.get,
                    Scalr.Method.BALANCED,
                    Scalr.Mode.FIT_TO_WIDTH,
                    sizedImageTargetWidth,
                    originalBufferedImage.get.getHeight))
            sizedImageWidth = sizedBufferedImage.get.getWidth
            sizedImageHeight = sizedBufferedImage.get.getHeight

            val sizedOutputStream = new ByteArrayOutputStream(sizedBufferedImage.get.getWidth * sizedBufferedImage.get.getHeight * 4)
            val sizedBytes = if (ImageIO.write(sizedBufferedImage.get, imageTargetFormat, sizedOutputStream)) {
                sizedOutputStream.toByteArray
            } else {
                throw new ImageException("Failed to resize image %s" format(image.imageOriginalUrl))
            }

            val sizedFileName = "%s-%sx%s.%s" format(
                    baseFileName,
                    sizedImageWidth,
                    sizedImageHeight,
                    imageTargetFormat)

            val sizedImageUploadCallback: Either[Throwable, String] => Unit = _.fold(
                e => {
                    logger.error("Error storing resized image %s with name %s" format(image.imageOriginalUrl, sizedFileName), e)
                    me ! SizedImageUploadError(e)
                },
                url => {
                    logger.debug("Successfully stored resized image {} at {}", image.imageOriginalUrl, url)
                    me ! SizedImageUploaded(url)
                })

            logger.debug("Saving resized image of {} as {}", image.imageOriginalUrl, sizedFileName)
            blobStore.store(
                sizedBytes,
                sizedFileName,
                "image/%s" format imageTargetFormat,
                sizedImageUploadCallback,
                metadata)



            logger.debug("Creating thumbnail of original image {}", image.imageOriginalUrl)
            thumbBufferedImage = Some(Scalr.resize(
                originalBufferedImage.get,
                Scalr.Method.BALANCED,
                Scalr.Mode.FIT_TO_WIDTH,
                thumbImageTargetWidth,
                originalBufferedImage.get.getHeight))

            thumbCroppedBufferedImage = if (thumbBufferedImage.get.getHeight > thumbImageTargetHeight) {
                logger.debug("Cropping thumbnail of original image {}", image.imageOriginalUrl)
                Some(Scalr.crop(
                    thumbBufferedImage.get,
                    thumbBufferedImage.get.getWidth,
                    thumbImageTargetHeight))
            } else thumbBufferedImage


            val thumbOutputStream = new ByteArrayOutputStream(sizedBufferedImage.get.getWidth * sizedBufferedImage.get.getHeight * 4)
            val thumbBytes = if (ImageIO.write(thumbCroppedBufferedImage.get, imageTargetFormat, thumbOutputStream)) {
                thumbOutputStream.toByteArray
            } else {
                throw new ImageException("Failed to create thumbnail for image %s" format(image.imageOriginalUrl))
            }

            val thumbFileName = "%s-%sx%s.%s" format(
                baseFileName,
                thumbCroppedBufferedImage.get.getWidth,
                thumbCroppedBufferedImage.get.getHeight,
                imageTargetFormat)


            val thumbImageUploadCallback: Either[Throwable, String] => Unit = _.fold(
                e => {
                    logger.error("Error storing resized image %s with name %s" format(image.imageOriginalUrl, sizedFileName), e)
                    me ! ThumbImageUploadError(e)
                },
                url => {
                    logger.debug("Successfully stored resized image {} at {}", image.imageOriginalUrl, url)
                    me ! ThumbImageUploaded(url)
                })

            logger.debug("Saving thumbnail image of {} as {}", image.imageOriginalUrl, thumbFileName)
            blobStore.store(
                thumbBytes,
                thumbFileName,
                "image/%s" format imageTargetFormat,
                thumbImageUploadCallback,
                metadata)
        } catch {
            case e => logger.error("Error processing image %s" format image.imageOriginalUrl, e)
        } finally {
            originalInputStream.foreach(_.close)
            originalBufferedImage.foreach(_.flush)
            sizedBufferedImage.foreach(_.flush)
            thumbBufferedImage.foreach(_.flush)
            thumbCroppedBufferedImage.foreach(_.flush)
        }
    }


    protected def receive = {

        case msg @ GrabImage(img) =>
            val channel: Channel[GrabImageResponse] = self.channel

            //sanity check
            if (img.imageOriginalUrl != image.imageOriginalUrl) {
                val e = ImageException("Wrong image")
                logger.error("Wrong image %s should be %s" format(img.imageOriginalUrl, image.imageOriginalUrl), e)
                channel ! GrabImageResponse(msg, Left(e))
            } else if (image.isGrabbed) {
                //we've already processed successfully
                channel ! GrabImageResponse(msg, Right(image))
            } else if (grabException != None) {
                //we've already failed
                channel ! GrabImageResponse(msg, Left(grabException.get))
            } else {
                //add to the list of responses to send
                if (responses == None) {
                    responses = Some(ListBuffer[(GrabImage, Channel[GrabImageResponse])]())
                }
                responses.get += ((msg, channel))
            }


        case msg @ OriginalImageUploaded(url) =>
            logger.debug("Successfully uploaded original image {} to {}", image.imageOriginalUrl, url)

            //we do not want to stomp on the sized image info if there...
            if (!image.isProcessed) {
                image = image.copy(
                    imageUrl = url,
                    imageWidth = originalImageWidth,
                    imageHeight = originalImageHeight,
                    imageGrabbedOn = new Date,
                    imageGrabbedStatus = "grabbed %s" format url)
                imageDao.updateAllUnprocessed(image)

                responses.foreach { _.foreach { tuple =>
                    val message = tuple._1
                    val channel = tuple._2
                    channel tryTell GrabImageResponse(message, Right(image))
                }}
                responses = None
            }


        case msg @ OriginalImageUploadError(error) =>
            logger.error("Error uploading original image %s" format image.imageOriginalUrl, error)

            //if we have already processed (error or not) then don't update the image info
            if (!image.isProcessed) {
                image = image.copy(imageGrabbedOn = null, imageGrabbedStatus = error.getMessage.take(254))
                grabException = Some(GrabImageException(image, c = error))
                imageDao.update(image)

                responses.foreach { _.foreach { tuple =>
                    val message = tuple._1
                    val channel = tuple._2
                    channel tryTell GrabImageResponse(message, Left(grabException.get))
                }}
                responses = None
            }


        case msg @ SizedImageUploaded(url) =>
            logger.debug("Successfully uploaded resized image {} of {}", url, image.imageOriginalUrl)

            //we prefer the sized image so always update with its info if we get it...
            image = image.copy(
                imageUrl = url,
                imageWidth = sizedImageWidth,
                imageHeight = sizedImageHeight,
                imageGrabbedOn = new Date,
                imageGrabbedStatus = "grabbed %s" format url)
            imageDao.updateAll(image)

            responses.foreach { _.foreach { tuple =>
                val message = tuple._1
                val channel = tuple._2
                channel tryTell GrabImageResponse(message, Right(image))
            }}
            responses = None



        case msg @ SizedImageUploadError(error) =>
            //we currently do not care about errors uploading the resize image...
            logger.error("Error uploading resized image of %s" format image.imageOriginalUrl, error)


        case msg @ ThumbImageUploaded(url) =>
            //we currently do not care about the thumbnail...
            logger.debug("Successfully uploaded thumbnail image {} of {}", url, image.imageOriginalUrl)


        case msg @ ThumbImageUploadError(error) =>
            //we currently do not care about the thumbnail...
            logger.error("Error uploading thumbnail image of %s" format image.imageOriginalUrl, error)

    }
}

