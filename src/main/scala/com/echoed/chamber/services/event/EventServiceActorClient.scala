package com.echoed.chamber.services.event

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.ActorClient
import java.util.{Map => JMap}
import com.echoed.chamber.domain.EchoedUser


class EventServiceActorClient extends EventService with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    def facebookCanvasViewed(echoedUser: EchoedUser) {
        actorRef ! FacebookCanvasViewed(echoedUser)
    }

    def exhibitViewed(echoedUser: EchoedUser) {
        actorRef ! ExhibitViewed(echoedUser)
    }

    def widgetRequested(partnerId: String){
        actorRef ! WidgetRequested(partnerId)
    }

    def widgetOpened(partnerId: String){
        actorRef ! WidgetOpened(partnerId)
    }

    def widgetStoryOpened(storyId: String){
        actorRef ! WidgetStoryOpened(storyId)
    }
}
