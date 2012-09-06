package com.echoed.chamber.services.state

import org.squeryl.PrimitiveTypeMode._
import com.echoed.chamber.services.state.schema.ChamberSchema._
import com.echoed.chamber.services._
import javax.sql.DataSource
import scalaz._
import Scalaz._
import com.echoed.chamber.services.echoeduser._
import java.util.Date
import com.echoed.util.DateUtils._
import com.echoed.chamber.services.state.schema.{Schedule, EchoedUserSettings, Notification}
import scala.Left
import com.echoed.chamber.services.echoeduser.EchoedUserUpdated
import scala.Right
import com.echoed.chamber.services.echoeduser.EchoedUserCreated
import com.echoed.chamber.services.echoeduser.NotificationCreated
import com.echoed.chamber.domain
import com.echoed.chamber.domain.EchoedUser
import scala.collection.immutable.Stack
import com.echoed.chamber.services.scheduler.{ScheduleDeleted, ScheduleCreated}


class StateService(
        val ep: EventProcessorActorSystem,
        val dataSource: DataSource) extends EchoedService with SquerylSessionFactory {

    ep.subscribe(context.self, classOf[CreatedEvent])
    ep.subscribe(context.self, classOf[UpdatedEvent])
    ep.subscribe(context.self, classOf[DeletedEvent])


    def readNotifications(eu: Option[EchoedUser]) =
        eu
            .map(eu => (from(notifications)(n =>
                    where(n.echoedUserId === eu.id)
                    select(n)
                    orderBy(n.createdOn asc))).toList)
            .map(_.map(_.convertTo(eu.get)))
            .map(Stack[domain.Notification]().pushAll(_))
            .getOrElse(Stack[domain.Notification]())

    def readEchoedUserSettings(eu: Option[EchoedUser]) = eu.map { eu =>
            (from(echoedUserSettings)(eus => where(eus.echoedUserId === eu.id) select(eus))).single.convertTo(eu)
    }


    protected def handle = {
        case msg @ ReadForEmail(email) => inTransaction {
            val eu = from(echoedUsers)(eu => where(eu.email === email) select(eu)).headOption
            val eus = readEchoedUserSettings(eu)
            val fu = eu.map(_.facebookUserId).flatMap(facebookUsers.lookup(_))
            val tu = eu.map(_.twitterUserId).flatMap(twitterUsers.lookup(_))
            val nf = readNotifications(eu)

            eu.cata(
                _ => sender ! ReadForEmailResponse(msg, Right(EchoedUserServiceState(eu.get, eus.get, fu, tu, nf))),
                sender ! ReadForEmailResponse(msg, Left(EchoedUserNotFound(email))))
        }

        case msg @ ReadForCredentials(credentials) => inTransaction {
            echoedUsers.lookup(credentials.id).foreach { eu =>
                val eus = readEchoedUserSettings(Option(eu)).get
                val fu = Option(eu.facebookUserId).flatMap(facebookUsers.lookup(_))
                val tu = Option(eu.twitterUserId).flatMap(twitterUsers.lookup(_))
                val nf = readNotifications(Option(eu))

                context.sender ! ReadForCredentialsResponse(msg, Right(EchoedUserServiceState(eu, eus, fu, tu, nf)))
            }
        }

        case msg @ ReadForFacebookUser(facebookUser) => inTransaction {
            val fu = from(facebookUsers)(fu => where(fu.facebookId === facebookUser.facebookId) select(fu)).headOption
            val eu = fu.map(_.echoedUserId).flatMap(echoedUsers.lookup(_))
            val eus = readEchoedUserSettings(eu)
            val tu = eu.map(_.twitterUserId).flatMap(twitterUsers.lookup(_))
            val nf = readNotifications(eu)

            fu.cata(
                _ => context.sender ! ReadForFacebookUserResponse(msg, Right(EchoedUserServiceState(eu.get, eus.get, fu, tu, nf))),
                context.sender ! ReadForFacebookUserResponse(msg, Left(FacebookUserNotFound(facebookUser))))
        }

        case msg @ ReadForTwitterUser(twitterUser) => inTransaction {
            val tu = from(twitterUsers)(tu => where(tu.twitterId === twitterUser.twitterId) select(tu)).headOption
            val eu = tu.map(_.echoedUserId).flatMap(echoedUsers.lookup(_))
            val eus = readEchoedUserSettings(eu)
            val fu = eu.map(_.facebookUserId).flatMap(facebookUsers.lookup(_))
            val nf = readNotifications(eu)

            tu.cata(
                _ => context.sender ! ReadForTwitterUserResponse(msg, Right(EchoedUserServiceState(eu.get, eus.get, fu, tu, nf))),
                context.sender ! ReadForTwitterUserResponse(msg, Left(TwitterUserNotFound(twitterUser))))
        }

        case EchoedUserCreated(echoedUser, eus, facebookUser, twitterUser) => inTransaction {
            echoedUsers.insert(echoedUser)
            echoedUserSettings.insert(EchoedUserSettings(eus))
            facebookUsers.insert(facebookUser)
            twitterUsers. insert(twitterUser)
        }

        case EchoedUserUpdated(echoedUser, eus, facebookUser, twitterUser) => inTransaction {
            echoedUsers.update(echoedUser.copy(updatedOn = new Date))
            echoedUserSettings.update(EchoedUserSettings(eus.copy(updatedOn = new Date)))
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

        case NotificationCreated(notification) => inTransaction {
            notifications.insert(Notification(notification))
        }

        case NotificationUpdated(notification) => inTransaction {
            notifications.update(Notification(notification.copy(updatedOn = new Date)))
        }

        case msg: ReadSchedulerServiceState => inTransaction {
            sender ! ReadSchedulerServiceStateResponse(
                    msg,
                    Right(from(schedules)(s => select(s)).map(s => (s.id, s.convertTo)).toMap))
        }

        case ScheduleCreated(schedule) => inTransaction {
            schedules.insert(Schedule(schedule))
        }

        case ScheduleDeleted(schedule) => inTransaction {
            schedules.delete(from(schedules)(s => where(s.id === schedule.id) select(s)))
        }

        case msg @ ReadPartnerUserForEmail(email) => inTransaction {
            from(partnerUsers)(pu => where(pu.email === email) select(pu)).headOption.cata(
                pu => sender ! ReadPartnerUserForEmailResponse(msg, Right(pu)),
                sender ! ReadPartnerUserForEmailResponse(msg, Left(PartnerUserNotFound(email))))
        }

        case msg @ ReadPartnerUserForCredentials(credentials) => inTransaction {
            partnerUsers.lookup(credentials.partnerUserId).foreach { pu =>
                sender ! ReadPartnerUserForCredentialsResponse(msg, Right(pu))
            }
        }

        case msg @ ReadAdminUserForEmail(email) => inTransaction {
            from(adminUsers)(au => where(au.email === email) select(au)).headOption.cata(
                au => sender ! ReadAdminUserForEmailResponse(msg, Right(au)),
                sender ! ReadAdminUserForEmailResponse(msg, Left(AdminUserNotFound(email))))
        }

        case msg @ ReadAdminUserForCredentials(credentials) => inTransaction {
            adminUsers.lookup(credentials.adminUserId).foreach { au =>
                sender ! ReadAdminUserForCredentialsResponse(msg, Right(au))
            }
        }

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