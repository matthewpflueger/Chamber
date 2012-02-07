package com.echoed.chamber.controllers

import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import java.util.Date
import com.echoed.chamber.domain.{RetailerSettings, Retailer, EchoPossibility}
import com.echoed.chamber.dao.{RetailerSettingsDao, RetailerDao, EchoPossibilityDao}
import com.echoed.chamber.util.DataCreator


class EchoHelper extends ShouldMatchers {

    @Autowired @BeanProperty var dataCreator: DataCreator = _
    @Autowired @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @Autowired @BeanProperty var retailerDao: RetailerDao = _
    @Autowired @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _

    def setupEchoPossibility(
            retailer: Retailer = dataCreator.retailer,
            retailerSettings: RetailerSettings = dataCreator.retailerSettings,
            customerId: String = "testRetailerCustomerId",
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

        assert(retailerSettings.retailerId == retailer.id)

        retailerDao.deleteByName(retailer.name)
        retailerSettingsDao.deleteByRetailerId(retailerSettings.retailerId)
        retailerSettingsDao.deleteById(retailerSettings.id)
        retailerDao.insert(retailer)
        retailerSettingsDao.insert(retailerSettings)

        echoPossibilityDao.deleteByRetailerId(retailer.id)
        val echoPossibility = new EchoPossibility(
                retailer.id,
                customerId,
                productId,
                boughtOn,
                step,
                orderId,
                price,
                imageUrl,
                echoedUserId,
                echoId,
                landingPageUrl,
                productName,
                category,
                brand,
                description,
                echoClickId);

        val count = echoPossibilityDao.selectCount
        (echoPossibility, count)
    }

    def validateEchoPossibility(echoPossibility: EchoPossibility, count: Long) {
        validateCountIs(count + 1)
        val recordedEchoPossibility = echoPossibilityDao.findById(echoPossibility.id)
        recordedEchoPossibility.id should not be (null)
        recordedEchoPossibility.step should equal (echoPossibility.step)
        recordedEchoPossibility.echoedUserId should equal (echoPossibility.echoedUserId)
        recordedEchoPossibility.echoId should equal (echoPossibility.echoId)
    }

    def getEchoPossibilityCount = echoPossibilityDao.selectCount

    def validateCountIs(count: Long) {
        //This is a nasty hack to allow time for the underlying database to be updated.  To repeat this bug start
        //Chamber up in debug mode with no breakpoints set and then run this test, if your machine is like mine the test
        //will pass the first time but fail the second time (of course it should always pass so you will have to force a database update).
        //Anyway, for some reason manually flushing the SQL statement caches does not work...
        Thread.sleep(1000)
        count should equal (echoPossibilityDao.selectCount)
    }
}
