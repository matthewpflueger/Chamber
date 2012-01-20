package com.echoed.chamber.dao


import java.util.{List => JList}
import com.echoed.chamber.domain.{EchoMetrics, Echo}


trait EchoMetricsDao {

    def insert(echoMetrics: EchoMetrics): Int

    def updateForClick(echoMetrics: EchoMetrics): Int

    def findByEchoId(echoId: String): EchoMetrics

    def findByEchoedUserId(echoedUserId: String): JList[EchoMetrics]

    def deleteByEchoId(echoId: String): Int

    def findById(id: String): EchoMetrics

    def findByRetailerSettingsId(retailerSettingsId: String): JList[EchoMetrics]

    def findByRetailerId(retailerId: String): JList[EchoMetrics]

    def deleteByRetailerSettingsId(retailerSettingsId: String): Int

    def deleteByRetailerId(retailerId: String): Int

    def deleteByEchoedUserId(retailerId: String): Int
}
