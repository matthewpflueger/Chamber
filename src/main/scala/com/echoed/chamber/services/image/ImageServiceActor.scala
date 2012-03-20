package com.echoed.chamber.services.image

import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import scalaz._
import Scalaz._
import com.echoed.chamber.dao._
import akka.dispatch.{PriorityGenerator, PriorityExecutorBasedEventDrivenDispatcher}
import com.echoed.util.BlobStore
import collection.mutable.ConcurrentMap
import com.echoed.cache.{CacheEntryRemoved, CacheListenerActorClient, CacheManager}
import akka.actor._


class ImageServiceActor extends Actor {

    val priority = PriorityGenerator {
        case GrabLowPriorityImage(_) => 100
        case _ => 0
    }

    self.dispatcher = new PriorityExecutorBasedEventDrivenDispatcher(classOf[ImageServiceActor].getSimpleName, priority)

    private val logger = LoggerFactory.getLogger(classOf[ImageServiceActor])


    @BeanProperty var cacheManager: CacheManager = _

    @BeanProperty var imageDao: ImageDao = _
    @BeanProperty var blobStore: BlobStore = _

    @BeanProperty var sizedImageTargetWidth = 230
    @BeanProperty var thumbImageTargetWidth = 120
    @BeanProperty var thumbImageTargetHeight = 120
    @BeanProperty var imageTargetFormat = "png"


    private var cache: ConcurrentMap[String, ActorRef] = null


    override def preStart() {
        cache = cacheManager.getCache[ActorRef]("ImageProcessors", Some(new CacheListenerActorClient(self)))

    }


    protected def receive = {
        case msg @ CacheEntryRemoved(originalImageUrl: String, imageProcessorActor: ActorRef, cause: String) =>
            logger.debug("Received {}", msg)
            imageProcessorActor.stop
            logger.debug("Stopped image processor {}", imageProcessorActor.id)

        case msg @ GrabImage(image) =>
            val channel: Channel[GrabImageResponse] = self.channel

            Option(imageDao.findProcessedByOriginalUrl(image.imageOriginalUrl)).cata(
                processedImage => {
                    logger.debug("Found already processed image for {}", image.imageOriginalUrl)
                    val i = image.copy(
                            imageUrl = processedImage.imageUrl,
                            imageWidth = processedImage.imageWidth,
                            imageHeight = processedImage.imageHeight,
                            imageGrabbedOn = processedImage.imageGrabbedOn,
                            imageGrabbedStatus = processedImage.imageGrabbedStatus)
                    imageDao.update(i)
                    channel ! GrabImageResponse(msg, Right(i))
                },
                {
                    logger.debug("Starting to process image {}", image.imageOriginalUrl)
                    cache.getOrElseUpdate(image.imageOriginalUrl, {
                        Actor.actorOf(new ImageProcessorActor(
                                image,
                                imageDao,
                                blobStore,
                                sizedImageTargetWidth,
                                thumbImageTargetWidth,
                                thumbImageTargetHeight,
                                imageTargetFormat)).start
                    }) forward msg
                })
    }
}

