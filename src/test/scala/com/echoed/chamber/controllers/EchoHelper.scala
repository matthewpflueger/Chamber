package com.echoed.chamber.controllers

import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import com.echoed.chamber.util.DataCreator
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}
import com.echoed.chamber.dao.EchoDao
import java.util.Date
import com.echoed.chamber.domain.Echo
import com.echoed.chamber.domain.partner.{PartnerSettings, Partner}


class EchoHelper extends ShouldMatchers {

    @Autowired @BeanProperty var dataCreator: DataCreator = _
    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var partnerDao: PartnerDao = _
    @Autowired @BeanProperty var partnerSettingsDao: PartnerSettingsDao = _

    def setupEchoPossibility(
            partner: Partner = dataCreator.partner,
            partnerSettings: PartnerSettings = dataCreator.partnerSettings,
            customerId: String = "testPartnerCustomerId",
            productId: String = "testProductId",
            boughtOn: Date = new Date(1320871016126L), //Wed Nov 09 15:36:56 EST 2011,
            step: String = "button",
            orderId: String = "testOrderId",
            price: Int = 100, //one dollar
            imageUrl: String = dataCreator.imagesUrl + "/Pic1.jpg",
            echoedUserId: String = null,
            echoId: String = null,
            landingPageUrl: String = dataCreator.siteUrl,
            productName: String = "My Awesome Boots",
            category: String = "Footwear",
            brand: String = "Nike",
            description: String = "These are amazing boots",
            echoClickId: String = null) = {

        assert(partnerSettings.partnerId == partner.id)

        partnerDao.deleteByName(partner.name)
        partnerSettingsDao.deleteByPartnerId(partnerSettings.partnerId)
        partnerSettingsDao.deleteById(partnerSettings.id)
        partnerDao.insert(partner)
        partnerSettingsDao.insert(partnerSettings)

        echoDao.deleteByPartnerId(partner.id)
        var echoPossibility = Echo.make(
                partnerId = partner.id,
                customerId = customerId,
                productId = productId,
                boughtOn = boughtOn,
                step = step,
                orderId = orderId,
                price = price,
                imageUrl = imageUrl,
                landingPageUrl = landingPageUrl,
                productName = productName,
                category = category,
                brand = brand,
                description = description,
                echoClickId = echoClickId,
                browserId = null,
                ipAddress = null,
                userAgent = null,
                referrerUrl = null,
                partnerSettingsId = partnerSettings.id)

        echoPossibility = if (echoId != null) echoPossibility.copy(id = echoId) else echoPossibility
        echoPossibility = if (echoedUserId != null) echoPossibility.copy(echoedUserId = echoedUserId) else echoPossibility

        val count = echoDao.selectCount
        (echoPossibility, count)
    }

    def validateEchoPossibility(echoPossibility: Echo, count: Long) = {
        validateCountIs(count + 1)
        val recordedEchoPossibility = echoDao.findByEchoPossibilityId(echoPossibility.echoPossibilityId)
        recordedEchoPossibility.id should not be (null)
        recordedEchoPossibility.step contains (echoPossibility.step)
        if (recordedEchoPossibility.step.contains("echoed")) {
            recordedEchoPossibility.echoedUserId should not be(null)
        } else {
            recordedEchoPossibility.echoedUserId should be(null)
        }
        recordedEchoPossibility.echoPossibilityId should equal (echoPossibility.echoPossibilityId)
        recordedEchoPossibility
    }

    def getEchoPossibilityCount = echoDao.selectCount

    def validateCountIs(count: Long) {
        //This is a nasty hack to allow time for the underlying database to be updated.  To repeat this bug start
        //Chamber up in debug mode with no breakpoints set and then run this test, if your machine is like mine the test
        //will pass the first time but fail the second time (of course it should always pass so you will have to force a database update).
        //Anyway, for some reason manually flushing the SQL statement caches does not work...
        Thread.sleep(1000)
        count should equal (echoDao.selectCount)
    }
}
