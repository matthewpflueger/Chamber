package com.echoed.chamber

import dao.{RetailerDao, EchoPossibilityDao}
import domain.{EchoPossibilityHelper, EchoPossibility, Retailer}
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers


class EchoHelper extends ShouldMatchers {

    @Autowired @BeanProperty var echoPossibilityDao: EchoPossibilityDao = null
    @Autowired @BeanProperty var retailerDao: RetailerDao = null


    def setupEchoPossibility = {
        val (echoPossibility, _) = EchoPossibilityHelper.getValidEchoPossibilityAndHash()

        retailerDao.insertOrUpdate(new Retailer(echoPossibility.retailerId))
        echoPossibilityDao.deleteById(echoPossibility.id)
        val count = echoPossibilityDao.selectCount
        (echoPossibility, count)
    }

    def validateEchoPossibility(echoPossibility: EchoPossibility, count: Long) {
        validateCountIs(count + 1)
        val recordedEchoPossibility = echoPossibilityDao.findById(echoPossibility.id)
        recordedEchoPossibility.id should equal (echoPossibility.id)
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