package com.echoed.chamber.services.image

import scalaz._
import Scalaz._
import com.echoed.chamber.dao._
import com.echoed.util.BlobStore
import akka.actor._
import java.awt.image.BufferedImage
import io.Source
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest
import javax.imageio.ImageIO
import org.imgscalr.Scalr
import com.echoed.chamber.domain.Image
import collection.mutable.ListBuffer
import java.util.{Calendar, Date}
import java.io.{FileNotFoundException, ByteArrayOutputStream, ByteArrayInputStream}
import org.jclouds.rest.AuthorizationException
import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import akka.dispatch.Promise
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.ask


class ImageServiceActor(
        blobStore: BlobStore,
        imageDao: ImageDao,
        minimumValidImageWidth: Int = 120,
        sizedImageTargetWidth: Int = 230,
        thumbImageTargetWidth: Int = 120,
        thumbImageTargetHeight: Int = 120,
        findUnprocessedImagesInterval: Long = 60000,
        reloadBlobStoreInterval: Long = 60000,
        lastProcessedBeforeMinutes: Int = 5,
        implicit val timeout: Timeout = Timeout(20000)) extends Actor with ActorLogging {


    //only for testing purposes
    var future: Option[Promise[ProcessImageResponse]] = None
    var unprocessedFuture: Option[Promise[Option[Image]]] = None

    //responses is a concurrent map because the sendResponse call gets initiated post blobStore.store in a future...
    private val responses: ConcurrentMap[String, ListBuffer[(ProcessImage, ActorRef)]] =
        new ConcurrentHashMap[String, ListBuffer[(ProcessImage, ActorRef)]]()

    private val reloadBlobStore = new AtomicLong(System.currentTimeMillis())



    override def preStart() {
        val me = self
        me ! FindUnprocessedImage()

        //this is a hack to make sure we continue to process failed images as we have a bug (API-67) that causes the
        //original Scheduler call to never happen again if a low priority image bugs out :(
        context.system.scheduler.schedule(
                findUnprocessedImagesInterval milliseconds,
                findUnprocessedImagesInterval milliseconds,
                me,
                FindUnprocessedImage())
    }



    private def download(url: String) = {
        log.debug("Downloading image {}", url)
        Source.fromURL(url)(scala.io.Codec.ISO8859).map(_.toByte).toArray
    }


    private def processImage(
                image: Image,
                url: String,
                descriptor: String,
                downloader: String => Array[Byte] = (download _),
                sizer: BufferedImage => Option[BufferedImage] = (_ => None))(f: Either[Throwable, ImageInfo] => Unit) = {
        var bufferedImage: Option[BufferedImage] = None
        var sizedBufferedImage: Option[BufferedImage] = None

        try {
            if (!image.isValidFormat) throw InvalidImageFormat(image, image.ext)

            val contentType = "image/%s" format image.ext
            var bytes = downloader(url)
            val md5 = Base64.encodeBase64URLSafeString(MessageDigest.getInstance("MD5").digest(bytes))

            log.debug("Reading image {} from {}", descriptor, url)
            bufferedImage = Some(ImageIO.read(new ByteArrayInputStream(bytes)))
            log.debug("Read image {} from {}", descriptor, url)

            if (bufferedImage.get.getWidth < minimumValidImageWidth) {
                throw InvalidImageWidth(image, bufferedImage.get.getWidth)
            }

            log.debug("Sizing image {} from {}", descriptor, url)
            sizedBufferedImage = sizer(bufferedImage.get)
            log.debug("Sized image {} from {}", descriptor, url)

            bytes = sizedBufferedImage.map { bi =>
                val os = new ByteArrayOutputStream(bi.getWidth * bi.getHeight * 4)
                if (ImageIO.write(bi, image.ext, os)) os.toByteArray
                else throw new ProcessImageException(image, "Failed to write image %s" format url)
            }.getOrElse(bytes)

            val width = sizedBufferedImage.getOrElse(bufferedImage.get).getWidth
            val height = sizedBufferedImage.getOrElse(bufferedImage.get).getHeight
            val fileName = "%s-%sx%s-%s%s" format(
                    md5,
                    width,
                    height,
                    descriptor,
                    if (image.ext.length > 0) ".%s" format image.ext else "")

            val metadata = Some(Map[String, String]("url" -> image.url))

            f(Right(new ImageInfo(
                bytes,
                sizedBufferedImage.getOrElse(bufferedImage.get),
                fileName,
                width,
                height,
                contentType,
                metadata,
                md5,
                image.ext)))
        } catch {
            case e: FileNotFoundException => f(Left(ImageNotFound(image, _cause = e)))
            case e =>
                f(Left(e))
        } finally {
            bufferedImage.foreach(_.flush)
            sizedBufferedImage.foreach(_.flush)
        }
    }


    private def update(image: Image) = {
        try {
            val img = image.copy(processedOn = new Date, processedStatus = image.processedStatus.take(510))
            imageDao.update(img)
        } catch {
            case e => log.error("Error persisting {}, {}", image, e)
        }
    }

    private def sendResponse(image: Image, response: Either[ImageException, Image]) {
        update(image)

        responses.remove(image.url).orElse {
            log.debug("Nobody waiting for image processing response {}", response)
            None
        }.foreach(_.foreach { tuple =>
            val message = tuple._1
            val channel = tuple._2
            log.debug("Sending image processing response {}", response)
            channel ! ProcessImageResponse(message, response)
        })

        //only for testing purposes...
        future.foreach(_.success(ProcessImageResponse(ProcessImage(image), response)))
    }


    private def error(image: Image, e: Throwable, processedStatus: Option[String] = None) {
        log.info("Error processing {}, {}", image, e)

        val imageException =
                if (e.isInstanceOf[ImageException]) {
                    e.asInstanceOf[ImageException]
                } else {
                    ImageException(image, processedStatus.getOrElse("Error processing image"), e)
                }

        sendResponse(image.copy(processedStatus = imageException.getMessage, retries = image.retries + 1), Left(imageException))
    }


    private def store(image: Image, imageInfo: ImageInfo)(success: String => Unit) {
        val me = self

        blobStore.store(
                imageInfo.bytes,
                imageInfo.fileName,
                imageInfo.contentType,
                imageInfo.metadata).onComplete(_.fold(
            e => {
                //ugly hack to get the blob store to re-authorize at a maximum rate of once a reloadBlobStoreInterval
                //this was supposedly fixed in jclouds 1.3.0 but for the life of me I can't see to get it to work
                //http://code.google.com/p/jclouds/issues/detail?id=731&can=1&q=Authorization&sort=-milestone
                if (e.isInstanceOf[AuthorizationException]) {
                    log.debug("Received authorization error - will try to reload blob store")
                    val now = System.currentTimeMillis()
                    val previousReloadBlobStore = reloadBlobStore.get()
                    val nextReloadBlobStore = now + reloadBlobStoreInterval
                    if (now > previousReloadBlobStore && reloadBlobStore.compareAndSet(previousReloadBlobStore, nextReloadBlobStore)) {
                        log.debug("Sending ReloadBlobStore message")
                        me ! ReloadBlobStore(image, imageInfo, success)
                        return
                    }
                }
                me ! ImageStoreError(image, e, Some("Error storing image %s: %s" format(imageInfo.fileName, e.getMessage)))
            },
            storedUrl => success(storedUrl)))
    }


    protected def receive = {

        case msg @ ReloadBlobStore(image, imageInfo, success) =>
            log.debug("Reloading blob store")
            blobStore.destroy()
            blobStore.init()
            store(image, imageInfo)(success)


        case msg @ StartProcessImage(image) =>
            val me = context.self
            val channel = context.sender

            try {
                imageDao.insert(image)
                channel ! StartProcessImageResponse(msg, Right(image))
                (me ? ProcessImage(image)).onComplete(_.fold(
                    e => log.error("Error processing image {}: {}", image.url, e),
                    _ => log.debug("Successfully processed image {}", image.url)))
            } catch {
                case e =>
                    log.error("Error inserting image {}: {}", image, e)
                    channel ! StartProcessImageResponse(msg, Left(ImageException(image, "Error inserting image", e)))
            }

        case msg @ ProcessImage(image) =>
            val me = context.self
            val channel = context.sender

            log.debug("Starting to process {}", image.url)
            if (image.isProcessed) {
                log.debug("Image has already been processed {}", image)
                future.foreach(_.success(ProcessImageResponse(msg, Right(image))))
                channel ! ProcessImageResponse(msg, Right(image))
            } else /* commenting this out as it is really buggy - we have a leak that causes the response list to grow
                      indefinitely - see API-67 this is part of that whole thing...
                   responses.get(image.url).cata(
                //looks like we are already processing this image so lets just remember to respond later...
                list => {
                    list += ((msg, channel))
                    logger.debug("Already processing image {}, list of channels waiting for response {}", image.url, list)
                },   */
                {
                    val list = responses.getOrElseUpdate(image.url, { ListBuffer[(ProcessImage, ActorRef)]() })

                    //we are not working on the image so lets see what we need to do...
                    log.debug("Processing {}", image.url)
//                    val list = ListBuffer[(ProcessImage, Channel[ProcessImageResponse])]()
                    list += ((msg, channel))
//                    responses.put(image.url, list)


                    if (!image.hasOriginal) {
                        me ! ProcessOriginalImage(image)
                    } else if (!image.hasSized) {
                        me ! ProcessSizedImage(image)
                    } else if (!image.hasThumbnail) {
                        me ! ProcessThumbnailImage(image)
                    } else {
                        //sanity check...
                        log.error("Image already processed!?!? {}", image.url)
                        responses.remove(image.url)
                        channel ! ProcessImageResponse(msg, Right(image))
                    }
                } //)


        case msg @ ProcessOriginalImage(image) =>
            val me = self

            log.debug("Processing original image from {}", image.url)
            processImage(image, image.urlWithEncodedFileName, "original") {
                case Left(e) => error(image, e, Some("Error processing original image: %s" format e.getMessage))
                case Right(imageInfo) =>
                    log.debug("Storing original image {}", imageInfo.fileName)
                    store(image, imageInfo) { storedUrl =>
                        log.debug("Successfully stored original of {} at {}", image.url, storedUrl)
                        me ! ProcessSizedImage(image.copy(
                                originalUrl = storedUrl,
                                originalWidth = imageInfo.width,
                                originalHeight = imageInfo.height,
                                processedOn = new Date,
                                processedStatus = "processed"))
                    }
            }


        case msg @ ProcessSizedImage(image) =>
            val me = self

            log.debug("Processing sized image from {}", image.originalUrl)
            processImage(
                    image,
                    image.originalUrl,
                    "sized",
                    url => {
                        val bytes = download(url)
                        //we've successfully tested that our original image is there so let's update to prevent starting from scratch...
                        update(image)
                        bytes
                    },
                    bi => {
                        if (bi.getWidth == sizedImageTargetWidth) None
                        else Some(Scalr.resize(bi, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, sizedImageTargetWidth, bi.getHeight))
                    }) {

                case Left(e) => error(image, e, Some("Error processing sized image: %s" format e.getMessage))
                case Right(imageInfo) =>
                    log.debug("Storing sized image {}", imageInfo.fileName)
                    store(image, imageInfo) { storedUrl =>
                        log.debug("Successfully stored sized of {} at {}", image.originalUrl, storedUrl)
                        me ! ProcessThumbnailImage(image.copy(
                                sizedUrl = storedUrl,
                                sizedWidth = imageInfo.width,
                                sizedHeight = imageInfo.height,
                                processedOn = new Date,
                                processedStatus = "processed"))
                    }
            }


        case msg @ ProcessThumbnailImage(image) =>
            log.debug("Processing thumbnail image from {}", image.sizedUrl)
            processImage(
                    image,
                    image.sizedUrl,
                    "thumbnail",
                    url => {
                        val bytes = download(url)
                        //we've successfully tested that our sized image is there so let's update to prevent starting from scratch...
                        update(image)
                        bytes
                    },
                    bi => {
                        if (bi.getWidth == thumbImageTargetWidth && bi.getHeight == thumbImageTargetHeight) None
                        else Some(Scalr.resize(bi, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, thumbImageTargetWidth, thumbImageTargetHeight))
                    }) {

                case Left(e) => error(image, e, Some("Error processing thumbnamil image: %s" format e.getMessage))
                case Right(imageInfo) =>
                    log.debug("Storing thumbnail image {}", imageInfo.fileName)
                    store(image, imageInfo) { storedUrl =>
                        log.debug("Successfully stored thumbnail of {} at {}", image.sizedUrl, storedUrl)
                        val img = image.copy(
                            thumbnailUrl = storedUrl,
                            thumbnailWidth = imageInfo.width,
                            thumbnailHeight = imageInfo.height,
                            processedOn = new Date,
                            processedStatus = "processed")
                        sendResponse(img, Right(img))
                    }
            }


        case resp @ ProcessImageResponse(msg, result) =>
            val me = self

            resp.cata(
                log.error("Error processing low priority image {}: {}", msg.image.url, _),
                log.debug("Successfully processed low priority image {}", _))
            me ! FindUnprocessedImage()


        case msg @ ProcessLowPriorityImage(image) =>
            val me = self

            log.debug("Starting to process low priority image {}", image.url)
            me ! ProcessImage(image)


        case msg @ FindUnprocessedImage() =>
            val me = self

            if (findUnprocessedImagesInterval > 0 && lastProcessedBeforeMinutes > 0) {
                val lastProcessedBeforeDate = {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MINUTE, lastProcessedBeforeMinutes * -1)
                    cal.getTime
                }

                log.debug("Looking for unprocessed images last processed before {}", lastProcessedBeforeDate)
                Option(imageDao.findUnprocessed(lastProcessedBeforeDate)).cata(
                    image => {
                        me ! ProcessLowPriorityImage(image)
                        unprocessedFuture.foreach(_.success(Some(image)))
                    },
                    {
                        //Scheduler.scheduleOnce(me, FindUnprocessedImage(), findUnprocessedImagesInterval, TimeUnit.MILLISECONDS)
                        unprocessedFuture.foreach(_.success(None))
                    })
            } else {
                log.info(
                    "Not finding unprocessed images because findUnprocessedImagesInterval {} or lastProcessWithinMinutes {} less than or equal to zero",
                    findUnprocessedImagesInterval,
                    lastProcessedBeforeMinutes)
                unprocessedFuture.foreach(_.success(None))
            }


        case msg @ ImageStoreError(image, e, message) => error(image, e, message)

    }

}


