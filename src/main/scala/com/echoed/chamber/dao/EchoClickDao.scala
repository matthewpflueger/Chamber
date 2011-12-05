package com.echoed.chamber.dao

import com.echoed.chamber.domain.EchoClick

import java.util.{List => JList}

trait EchoClickDao {

    def insert(echoClick: EchoClick): Int

    def findByEchoId(echoId: String): JList[EchoClick]

    def deleteByEchoId(echoId: String): Int

}
