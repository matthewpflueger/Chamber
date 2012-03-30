package com.echoed.chamber.services.image

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain._
import com.echoed.chamber.services.ActorClient


class ImageServiceActorClient extends ImageService with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    def processImage(image: Image) =
        (actorRef ? ProcessImage(image)).mapTo[ProcessImageResponse]

}
