package com.echoed.chamber.services.adminuser

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

class AdminUserServiceLocatorActorClient extends AdminUserServiceLocator with ActorClient with Serializable {

    @BeanProperty var adminUserServiceLocatorActor: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def locateAdminUserService(email: String) =
        (adminUserServiceLocatorActor ? LocateAdminUserService(email)).mapTo[LocateAdminUserServiceResponse]
    
    def login(email:String, password: String) = 
        (adminUserServiceLocatorActor ? Login(email,password)).mapTo[LoginResponse]
    
    def logout(id:String) =
        (adminUserServiceLocatorActor ? Logout(id)).mapTo[LogoutResponse]
    
    def create(email:String, name: String, password: String) = 
        (adminUserServiceLocatorActor ? CreateAdminUser(email,name,password)).mapTo[CreateAdminUserResponse]

    def actorRef = adminUserServiceLocatorActor

}
