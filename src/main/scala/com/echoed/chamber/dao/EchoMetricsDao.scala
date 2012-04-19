package com.echoed.chamber.dao


import java.util.{List => JList}
import com.echoed.chamber.domain.EchoMetrics


trait EchoMetricsDao {

    def insert(echoMetrics: EchoMetrics): Int

    def updateForEcho(echoMetrics: EchoMetrics): Int

    def updateForClick(echoMetrics: EchoMetrics): Int

    def findByEchoId(echoId: String): EchoMetrics

    def findByEchoedUserId(echoedUserId: String): JList[EchoMetrics]

    def deleteByEchoId(echoId: String): Int

    def findById(id: String): EchoMetrics

    def findByPartnerSettingsId(partnerSettingsId: String): JList[EchoMetrics]

    def findByPartnerId(partnerId: String): JList[EchoMetrics]

    def deleteByPartnerSettingsId(partnerSettingsId: String): Int

    def deleteByPartnerId(partnerId: String): Int

    def deleteByEchoedUserId(partnerId: String): Int
}
