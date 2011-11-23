package com.echoed.chamber

import dao.{RetailerDao, EchoPossibilityDao}
import domain.{EchoPossibilityHelper, EchoPossibility, Retailer}
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import java.util.Date


class EchoHelper extends ShouldMatchers {

    @Autowired @BeanProperty var echoPossibilityDao: EchoPossibilityDao = null
    @Autowired @BeanProperty var retailerDao: RetailerDao = null


    def setupEchoPossibility(
            //a normal base64 will have one or more '=' characters for padding - they are ripped off for url safe base64 strings...
            expectedEchoPossibilityId: String = "dGVzdFJldGFpbGVySWR0ZXN0UmV0YWlsZXJDdXN0b21lcklkdGVzdFByb2R1Y3RJZFdlZCBOb3YgMDkgMTU6MzY6NTYgRVNUIDIwMTF0ZXN0T3JkZXJJZDEwMGh0dHA6Ly92MS1jZG4uZWNob2VkLmNvbS9lY2hvX2RlbW9fc3RvcmUtdGllX3RodW1iLmpwZWc",
            retailerId: String = "testRetailerId",
            customerId: String = "testRetailerCustomerId",
            productId: String = "testProductId",
            boughtOn: Date = new Date(1320871016126L), //Wed Nov 09 15:36:56 EST 2011,
            step: String = "button",
            orderId: String = "testOrderId",
            price: String = "100", //one dollar
            imageUrl: String = "http://v1-cdn.echoed.com/echo_demo_store-tie_thumb.jpeg",
            echoedUserId: String = null,
            echoId: String = null,
            landingPageUrl: String = "http://echoed.com") = {

        val (echoPossibility, _) = EchoPossibilityHelper.getValidEchoPossibilityAndHash(
                expectedEchoPossibilityId = expectedEchoPossibilityId,
                retailerId = retailerId,
                customerId = customerId,
                productId = productId,
                boughtOn = boughtOn,
                step = step,
                orderId = orderId,
                price = price,
                imageUrl = imageUrl,
                echoedUserId = echoedUserId,
                echoId = echoId,
                landingPageUrl = landingPageUrl);

        retailerDao.insertOrUpdate(new Retailer(echoPossibility.retailerId))
        echoPossibilityDao.deleteById(echoPossibility.id)
        val count = echoPossibilityDao.selectCount
        (echoPossibility, count)
    }

    def validateEchoPossibility(echoPossibility: EchoPossibility, count: Long) {
        validateCountIs(count + 1)
        val recordedEchoPossibility = echoPossibilityDao.findById(echoPossibility.id)
        recordedEchoPossibility.id should equal (echoPossibility.id)
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
