package com.echoed.chamber.controllers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.springframework.beans.factory.annotation.Autowired
import scala.reflect.BeanProperty
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import com.echoed.chamber.dao.views.ClosetDao
import java.util.{Date, Properties}
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.chamber.dao.{RetailerUserDao, EchoDao, EchoedUserDao}
import org.openqa.selenium.{By, Cookie, WebDriver}
import com.echoed.util.{WebDriverUtils, IntegrationTest}


/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/15/12
 * Time: 10:55 AM
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class AdminUserLoginIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var partnerUserDao: RetailerUserDao = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    var dashboardUrl: String = _

    {
        dashboardUrl = urls.getProperty("dashboardUrl")
        dashboardUrl != null
    } ensuring (_ == true, "Missing parameters")

}
