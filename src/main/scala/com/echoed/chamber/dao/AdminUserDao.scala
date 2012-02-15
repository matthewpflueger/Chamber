package com.echoed.chamber.dao

import com.echoed.chamber.domain.AdminUser
import com.echoed.chamber.domain.EchoedUser
import java.util.{List => JList}

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/3/12
 * Time: 10:01 AM
 * To change this template use File | Settings | File Templates.
 */

trait AdminUserDao {

    def insert(adminUser: AdminUser): Int

    def findByEmail(email: String): AdminUser

    def deleteByEmail(email: String): Int
    
}
