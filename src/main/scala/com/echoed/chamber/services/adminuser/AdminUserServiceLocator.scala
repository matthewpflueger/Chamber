package com.echoed.chamber.services.adminuser

import akka.dispatch.Future

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/2/12
 * Time: 9:16 PM
 * To change this template use File | Settings | File Templates.
 */

trait AdminUserServiceLocator {

    def locateAdminUserService(email: String): Future[LocateAdminUserServiceResponse]
    
    def login(email:String,  password: String): Future[LoginResponse]
    
    def logout(id: String): Future[LogoutResponse]
    
    def create(email:String, name:String,  password: String): Future[CreateAdminUserResponse]

}
