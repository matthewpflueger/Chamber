package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.EchoedUser
import scala.collection.JavaConversions

case class Closet(
        id: String,
        echoedUser: EchoedUser,
        echoes: JList[EchoView],
        totalCredit: Float) {

    def this(id: String, echoedUser: EchoedUser) = this(id, echoedUser, new ArrayList[EchoView](), 0f)
    
    def convertEchoesToEchoViewPublic = {
        JavaConversions.asJavaCollection(JavaConversions.asScalaBuffer(echoes).map { new EchoViewPublic(_) })
    }

}
