package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList, Collection=>JCollection}
import com.echoed.chamber.domain.EchoedUser
import scala.collection.JavaConversions

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/10/12
 * Time: 12:11 PM
 * To change this template use File | Settings | File Templates.
 */

case class ClosetPersonal(
                id: String,
                echoedUser: EchoedUser,
                echoes: JCollection[EchoViewPersonal],
                stories: JCollection[StoryFull],
                totalCredit: Float) {
    
    def this(id:String,  echoedUser: EchoedUser) = this(id,echoedUser,new ArrayList[EchoViewPersonal](), new ArrayList[StoryFull], 0f)

    def this(closet: Closet) = this(closet.id,
                                    closet.echoedUser,
                                    closet.convertEchoesToEchoViewPersonal,
                                    closet.stories,
                                    closet.totalCredit)
}
