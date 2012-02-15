package com.echoed.chamber.services.adminuser

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient

class AdminUserServiceActorClient(adminUserServiceActor: ActorRef) extends AdminUserService with ActorClient {

    def actorRef = adminUserServiceActor
    
    def getUsers =
        (adminUserServiceActor ? GetUsers()).mapTo[GetUsersResponse]
    
    def getEchoPossibilities =
        (adminUserServiceActor ? GetEchoPossibilities()).mapTo[GetEchoPossibilitesResponse]

    def getAdminUser =
        (adminUserServiceActor ? GetAdminUser()).mapTo[GetAdminUserResponse]

    def logout(adminUserId: String) =
        (adminUserServiceActor ? Logout(adminUserId)).mapTo[LogoutResponse]

    val id = actorRef.id

    override def toString = id

}
