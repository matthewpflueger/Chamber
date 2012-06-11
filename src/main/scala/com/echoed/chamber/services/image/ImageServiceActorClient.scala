package com.echoed.chamber.services.image

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain._
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class ImageServiceActorClient extends ImageService with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def processImage(image: Image) =
        (actorRef ? ProcessImage(image)).mapTo[ProcessImageResponse]

}
