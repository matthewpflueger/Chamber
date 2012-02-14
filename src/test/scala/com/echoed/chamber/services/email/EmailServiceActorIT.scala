package com.echoed.chamber.services.email

import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestActorRef
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, Spec, GivenWhenThen}
import com.echoed.util.IntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.util.{UUID, Map, Locale, Properties, HashMap => JHashMap}
import org.slf4j.LoggerFactory
import javax.mail.{Flags, Folder, Session}
import scala.reflect.BeanProperty
import com.samskivert.mustache.Template
import com.echoed.util.mustache.{MustacheEngine, MustacheView, MustacheViewResolver}

@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:emailIT.xml"))
class EmailServiceActorIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    private val logger = LoggerFactory.getLogger(classOf[EmailServiceActorIT])

    @Autowired @BeanProperty var javaMailSender: JavaMailSender = _
    @Autowired @BeanProperty var mailProperties: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    feature("Send email") {

        info("As a service on the system")
        info("I want to send email")
        info("So that I can communicate via email")

        scenario("the email service receives a request to send email", IntegrationTest) {
            given("a request to send email")
            when("it has valid information")
            then("send the email")

            val testModel = new JHashMap[String, AnyRef]()
            val uuid = UUID.randomUUID().toString
            testModel.put("uuid", uuid)

            val actorRef = TestActorRef[EmailServiceActor]
            actorRef.underlyingActor.from = "no-reply-dev@echoed.com"
            actorRef.underlyingActor.javaMailSender = javaMailSender
            actorRef.underlyingActor.mustacheEngine = new MustacheEngine {
                override def compile(templateName: String) = {
                    templateName should equal(uuid)

                    new Template(null, null) {
                        override def execute(context: AnyRef) = {
                            context should equal(testModel)
                            uuid
                        }
                    }
                }
            }

            actorRef.start()
            actorRef.isRunning should be (true)

            val msg = SendEmail("no-reply-dev@echoed.com", uuid, uuid, testModel)
            val response = actorRef.ask(msg).as[SendEmailResponse].get
            response.resultOrException should be(true)

            val properties = new Properties()
            properties.setProperty("mail.store.protocol", "imaps")
            val session = Session.getDefaultInstance(properties, null)
            val store = session.getStore("imaps")
            store.connect(
                mailProperties.getProperty("mail.imap.host"),
                mailProperties.getProperty("mail.user"),
                mailProperties.getProperty("mail.password"))


            val inbox = store.getFolder("Inbox")
            inbox.open(Folder.READ_WRITE)
            val messages = inbox.getMessages.filter(_.getSubject == uuid)
            messages.isEmpty should not be(true)
            messages should have length(1)
            messages.foreach(_.setFlag(Flags.Flag.DELETED, true))
            //NOTE: the above doesn't actually delete if it is Gmail just archives it
            //see http://tech.davemx.com/2010/move-gmail-message-to-trash-with-javamail/
        }

    }
}
