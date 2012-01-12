package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList,Collection => JCollection}
import com.echoed.chamber.domain.EchoedUser


case class FriendCloset (
                id:String,
                echoedUserName: String, 
                echoes: JCollection[EchoViewPublic]){

    def this(closet: Closet) = this(closet.id,
                                    closet.echoedUser.name,
                                    closet.convertEchoesToEchoViewPublic)

}