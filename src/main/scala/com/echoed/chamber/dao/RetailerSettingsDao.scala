package com.echoed.chamber.dao

import com.echoed.chamber.domain.RetailerSettings
import java.util.{Date, List => JList}
import org.apache.ibatis.annotations.Param


trait RetailerSettingsDao {

    def findById(id: String): RetailerSettings

    def findByRetailerId(retailerId: String): JList[RetailerSettings]

    def findByActiveOn(
            @Param("retailerId") retailerId: String,
            @Param("activeOn") activeOn: Date): RetailerSettings

    def insert(retailerSettings: RetailerSettings): Int

    def deleteByRetailerId(retailerId: String): Int

    def deleteById(id: String): Int
}




