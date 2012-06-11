package com.echoed.chamber.services.adminuser

import akka.dispatch.Future


trait AdminUserServiceLocator {

    def locateAdminUserService(email: String): Future[LocateAdminUserServiceResponse]
    
    def login(email:String,  password: String): Future[LoginResponse]
    
    def logout(id: String): Future[LogoutResponse]
    
    def create(email:String, name:String,  password: String): Future[CreateAdminUserResponse]

}
