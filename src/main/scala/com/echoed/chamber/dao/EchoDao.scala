package com.echoed.chamber.dao

import com.echoed.chamber.domain.Echo


trait EchoDao {

    def insertOrUpdate(echo: Echo): Int

    def findByEchoPossibilityId(echoPossibilityId: String): Echo
}
