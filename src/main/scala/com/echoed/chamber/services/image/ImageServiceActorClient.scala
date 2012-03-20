package com.echoed.chamber.services.image

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain._
import com.echoed.chamber.services.ActorClient


class ImageServiceActorClient extends ImageService with ActorClient {

    @BeanProperty var imageServiceActor: ActorRef = _

    def grabImage(image: Image) =
        (imageServiceActor ? GrabImage(image)).mapTo[GrabImageResponse]

    def actorRef = imageServiceActor
}
