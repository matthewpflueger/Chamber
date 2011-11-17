package com.echoed.chamber.dao

import com.echoed.chamber.domain.EchoPossibility


trait EchoPossibilityDao {

    def findById(id: String): EchoPossibility

    def insertOrUpdate(echoPossibility: EchoPossibility): Int

    def selectCount: Long

    def deleteById(id: String): Int

}
