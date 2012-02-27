package com.echoed.chamber.dao

import com.echoed.chamber.domain.EchoPossibility

import java.util.{List => JList}

trait EchoPossibilityDao {

    def findById(id: String): EchoPossibility

    def findByIdOrEchoId(id:String): EchoPossibility

    def findByRetailerId(id: String): JList[EchoPossibility]

    def insertOrUpdate(echoPossibility: EchoPossibility): Int

    def selectCount: Long

    def deleteById(id: String): Int

    def deleteByRetailerId(retailerId: String): Int

}
