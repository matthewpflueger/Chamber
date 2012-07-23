package com.echoed.chamber.services.state

import org.squeryl._
import org.squeryl.PrimitiveTypeMode._
import com.echoed.chamber.domain.ChamberSchema._
import com.echoed.chamber.services._
import javax.sql.DataSource
import org.squeryl.adapters.MySQLAdapter
import scalaz._
import Scalaz._
import scala.Left
import scala.Some
import scala.Right
import com.echoed.chamber.services.adminuser.AdminUserCreated
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials, EchoedUserUpdated, EchoedUserCreated}
import java.util.Date
import com.echoed.util.DateUtils._


class StateService(
        ep: EventProcessorActorSystem,
        dataSource: DataSource) extends EchoedService {

    SessionFactory.concreteFactory = Some(() => {
        val session = Session.create(dataSource.getConnection, new MySQLAdapter)
        session.setLogger(msg => log.debug(msg))
        session
    })

    ep.subscribe(context.self, classOf[CreatedEvent])
    ep.subscribe(context.self, classOf[UpdatedEvent])
    ep.subscribe(context.self, classOf[DeletedEvent])

    protected def handle = {
        case AdminUserCreated(adminUser) => inTransaction {
            adminUsers.insert(adminUser)
        }

        case msg @ ReadAdminUserServiceManagerState() => inTransaction {
            context.sender ! ReadAdminUserServiceManagerStateResponse(
                    msg,
                    Right(from(adminUsers)(au => select(au.email, au.id)).toMap))
        }

        case msg @ ReadForFacebookUser(facebookUser) => inTransaction {
            val fu = from(facebookUsers)(fu => where(fu.facebookId === facebookUser.facebookId) select(fu)).headOption
            val eu = fu.map(_.echoedUserId).flatMap(echoedUsers.lookup(_))
            val tu = eu.map(_.twitterUserId).flatMap(twitterUsers.lookup(_))

            fu.cata(
                _ => context.sender ! ReadForFacebookUserResponse(msg, Right(EchoedUserServiceState(eu.get, fu, tu))),
                context.sender ! ReadForFacebookUserResponse(msg, Left(FacebookUserNotFound(facebookUser))))
        }

        case EchoedUserCreated(echoedUser, facebookUser, twitterUser) => inTransaction {
            echoedUsers.insert(echoedUser)
            facebookUsers.insert(facebookUser)
            twitterUsers. insert(twitterUser)
        }

        case EchoedUserUpdated(echoedUser, facebookUser, twitterUser) => inTransaction {
            echoedUsers.update(echoedUser.copy(updatedOn = new Date))
            facebookUser.foreach { fu =>
                facebookUsers.lookup(fu.id)
                    .map(fu => facebookUsers.update(fu.copy(updatedOn = new Date)))
                    .orElse(Option(facebookUsers.insert(fu)))
            }
            twitterUser.foreach { tu =>
                twitterUsers.lookup(tu.id)
                    .map(tu => twitterUsers.update(tu.copy(updatedOn = new Date)))
                    .orElse(Option(twitterUsers.insert(tu)))
            }
        }

        case msg @ ReadForCredentials(credentials) => inTransaction {
            echoedUsers.lookup(credentials.id).foreach { eu =>
                val fu = Option(eu.facebookUserId).flatMap(facebookUsers.lookup(_))
                val tu = Option(eu.twitterUserId).flatMap(twitterUsers.lookup(_))
                context.sender ! ReadForCredentialsResponse(msg, Right(EchoedUserServiceState(eu, fu, tu)))
            }
        }

/*
private[services] case class ReadForFacebookUser(facebookUser: FacebookUser) extends REUSS
private[services] case class ReadForFacebookUserResponse(
                message: ReadForFacebookUser,
                value: Either[SE, EchoedUserServiceState])
                extends SM with MR[EchoedUserServiceState, ReadForFacebookUser, SE]

private[services] case class ReadForTwitterUser(twitterUser: TwitterUser) extends REUSS
private[services] case class ReadForTwitterUserResponse(
                message: ReadForTwitterUser,
                value: Either[SE, EchoedUserServiceState])
                extends SM with MR[EchoedUserServiceState, ReadForTwitterUser, SE]

private[services] case class EchoedUserServiceStateCreated(
                echoedUser: EchoedUser,
                facebookUser: Option[FacebookUser] = None,
                twitterUser: Option[TwitterUser] = None) extends SC

private[services] case class EchoedUserServiceStateUpdated(
                echoedUser: EchoedUser,
                facebookUser: Option[FacebookUser],
                twitterUser: Option[TwitterUser]) extends SC
//        case UpdateAdminUserServiceState(adminUser) =>
*/
    }
}


/*
                    transactionTemplate.execute({status: TransactionStatus =>

+package com.echoed.chamber.services
+
+import javax.sql.DataSource
+import org.squeryl._
+import org.squeryl.dsl._
+//import org.squeryl.{Session, SessionFactory}
+import org.squeryl.adapters.MySQLAdapter
+import org.squeryl.PrimitiveTypeMode._
+
+import com.echoed.chamber.domain.ChamberSchema._
+import com.echoed.chamber.domain.{Story, Chapter}
+
+class QueryService(dataSource: DataSource) {
+
+    SessionFactory.concreteFactory = Some(() => Session.create(
+            dataSource.getConnection,
+            new MySQLAdapter))
+
+    def logger(sql: String) {
+        println(sql)
+    }
+
+    def selectAllChapters: List[Chapter] = inTransaction {
+        Session.currentSession.setLogger(logger _)
+        from(chapters)(select(_)).toList
+    }
+
+    def selectChapterIds: List[String] = inTransaction {
+        Session.currentSession.setLogger(logger _)
+        from(chapters)(c => select(c.id)).toList
+    }
+
+    def selectAllStories: List[(Story, Option[Chapter])] = inTransaction {
+        Session.currentSession.setLogger(logger _)
+        join(stories, chapters.leftOuter)((s, c) =>
+            select((s, c))
+            on(s.id === c.get.storyId)).toList
+    }
+
+    def selectAllStoriesWithId(id: String): List[(Story, Option[Chapter])] = inTransaction {
+        Session.currentSession.setLogger(logger _)
+        join(stories, chapters.leftOuter)((s, c) =>
+            where(s.id === id)
+            select((s, c))
+            on(s.id === c.get.storyId)).toList
+    }
+
+}

*/