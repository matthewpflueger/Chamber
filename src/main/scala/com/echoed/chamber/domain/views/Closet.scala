package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.EchoedUser
import scala.collection.JavaConversions

case class Closet(
        id: String,
        echoedUser: EchoedUser,
        echoes: JList[EchoView],
        stories: JList[StoryFull],
        totalCredit: Float) {

    //as this is a view the id is the EchoedUser.id
    def this(id: String, echoedUser: EchoedUser) = this(id, echoedUser, new ArrayList[EchoView](), new ArrayList[StoryFull], 0f)

    def this(id: String, echoedUser: EchoedUser, echoes: JList[EchoView]) = this(id, echoedUser, echoes, new ArrayList[StoryFull], 0f)

    def this(id: String, echoedUser: EchoedUser, echoes: JList[EchoView], totalCredit: Float) = this(id, echoedUser, echoes, new ArrayList[StoryFull], totalCredit)


    def convertEchoesToEchoViewPublic = {
        JavaConversions.asJavaCollection(JavaConversions.asScalaBuffer(echoes).map { new EchoViewPublic(_) })
    }
    
    def convertEchoesToEchoViewPersonal = {
        JavaConversions.asJavaCollection(JavaConversions.asScalaBuffer(echoes).map { new EchoViewPersonal(_)})
    }

}
