package com.echoed.chamber.services.adminuser

import reflect.BeanProperty
import akka.actor.ActorRef

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/2/12
 * Time: 10:08 PM
 * To change this template use File | Settings | File Templates.
 */

class AdminUserServiceCreatorActorClient extends AdminUserServiceCreator{

    @BeanProperty var adminUserServiceCreatorActor: ActorRef = _

    def createAdminUserService(email: String) =
        (adminUserServiceCreatorActor ? CreateAdminUserService(email)).mapTo[CreateAdminUserServiceResponse]
    
    def createAdminUser(email:String, name:String, password:String) =
        (adminUserServiceCreatorActor ? CreateAdminUser(email,name,password)).mapTo[CreateAdminUserResponse]

}
