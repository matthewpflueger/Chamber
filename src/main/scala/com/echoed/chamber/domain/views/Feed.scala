package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.EchoedUser

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 12/13/11
 * Time: 9:12 PM
 * To change this template use File | Settings | File Templates.
 */

case class Feed(
        id: String, 
        echoedUser: EchoedUser,
        echoedFriend: EchoedUser,
        echoes: JList[EchoView]){
    
    def this(id:String, echoedUser: EchoedUser, echoedFriend: EchoedUser) = this(id, echoedUser, echoedFriend, new ArrayList[EchoView])
            
}

