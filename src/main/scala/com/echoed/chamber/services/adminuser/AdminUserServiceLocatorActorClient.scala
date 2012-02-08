package com.echoed.chamber.services.adminuser

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient

class AdminUserServiceLocatorActorClient extends AdminUserServiceLocator with ActorClient {

    @BeanProperty var adminUserServiceLocatorActor: ActorRef = _

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
