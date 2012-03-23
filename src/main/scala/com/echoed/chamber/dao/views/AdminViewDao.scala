package com.echoed.chamber.dao.views

import java.util.{List => JList}
import com.echoed.chamber.domain.{Echo, EchoedUser}


trait AdminViewDao {

    def getUsers: JList[EchoedUser]

    def getEchoPossibilities: JList[Echo]

}
