package com.echoed.chamber.services.adminuser
import akka.dispatch.Future

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/2/12
 * Time: 9:48 PM
 * To change this template use File | Settings | File Templates.
 */

trait AdminUserServiceCreator {

    def createAdminUserService(email: String): Future[CreateAdminUserServiceResponse]
    
    def createAdminUser(email:String,name:String, password:String): Future[CreateAdminUserResponse]

}
