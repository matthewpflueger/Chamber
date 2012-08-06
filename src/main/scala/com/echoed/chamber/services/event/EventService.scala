package com.echoed.chamber.services.event

import com.echoed.chamber.domain.EchoedUser

trait EventService {

    def facebookCanvasViewed(echoedUser: EchoedUser): Unit

    def exhibitViewed(echoedUser: EchoedUser): Unit

    def widgetRequested(partnerId: String): Unit

    def widgetOpened(partnerId: String): Unit

    def widgetStoryOpened(storyId: String): Unit

}
