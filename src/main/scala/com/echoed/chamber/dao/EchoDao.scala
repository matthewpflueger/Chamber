package com.echoed.chamber.dao

import com.echoed.chamber.domain.Echo

import java.util.{List => JList}


trait EchoDao {

    def insert(echo: Echo): Int

    def updateFacebookPostId(echo: Echo): Int

    def updateTwitterStatusId(echo: Echo): Int

    def findByEchoPossibilityId(echoPossibilityId: String): Echo

    def deleteByEchoPossibilityId(echoPossibilityId: String): Int

    def findById(id: String): Echo

    def findByRetailerId(retailerId: String): JList[Echo]

    def deleteByRetailerId(retailerId: String): Int

}
