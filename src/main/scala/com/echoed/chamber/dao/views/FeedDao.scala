package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.{Feed,EchoViewPublic}
import java.util.{List => JList}
import org.apache.ibatis.annotations.Param

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 12/13/11
 * Time: 9:14 PM
 * To change this template use File | Settings | File Templates.
 */

trait FeedDao {

    def findByEchoedUserId(
                            @Param("id") echoedUserId: String,
                            @Param("start") start: Int,
                            @Param("limit") limit: Int): Feed

    def getPublicFeed(
                            @Param("start")  start: Int,
                            @Param("limit") limit: Int): JList[EchoViewPublic]
    
    def getPartnerFeed(
                            @Param("partnerName") partnerName:String, 
                            @Param("start")  start: Int, 
                            @Param("limit") limit : Int) : JList[EchoViewPublic]
}