package com.echoed.chamber.controllers
import com.echoed.chamber.dao.{FacebookUserDao, EchoedUserDao}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import java.util.Properties
import org.openqa.selenium.{By, WebDriver}
import com.echoed.util.{WebDriverUtils, IntegrationTest}


/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/13/12
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */

class LogoutIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = null
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = null
    @Autowired @BeanProperty var echoHelper: EchoHelper = null
    @Autowired @BeanProperty var webDriver: WebDriver = null

    @Autowired @BeanProperty var urls: Properties = null

}