package com.echoed.chamber.dao

import com.echoed.chamber.domain.EchoClick


trait EchoClickDao {

    def insert(echoClick: EchoClick): Int

    def findByEchoId(echoId: String): EchoClick

}
