package com.echoed.chamber.services.adminuser

import com.echoed.chamber.domain.AdminUser
import akka.dispatch.Future
/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/2/12
 * Time: 9:10 PM
 * To change this template use File | Settings | File Templates.
 */

trait AdminUserService {

    val id: String

    def getAdminUser: Future[GetAdminUserResponse]
    
    def getUsers: Future[GetUsersResponse]
    
    def getEchoPossibilities: Future[GetEchoPossibilitesResponse]
    
    def logout(adminUserId: String): Future[LogoutResponse]

}
