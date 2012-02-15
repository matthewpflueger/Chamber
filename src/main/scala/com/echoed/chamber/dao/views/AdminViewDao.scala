package com.echoed.chamber.dao.views
import com.echoed.chamber.domain.{EchoedUser,EchoPossibility}
import java.util.{List => JList}


/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/6/12
 * Time: 10:34 AM
 * To change this template use File | Settings | File Templates.
 */

trait AdminViewDao {

    def getUsers: JList[EchoedUser]

    def getEchoPossibilities: JList[EchoPossibility]

}
