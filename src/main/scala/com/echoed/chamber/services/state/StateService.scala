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
import com.echoed.chamber.domain.{StoryState, EchoedUser}
import scala.collection.immutable.Stack
import com.echoed.chamber.services.scheduler.{ScheduleDeleted, ScheduleCreated}
import com.echoed.util.TransactionUtils._
import com.echoed.chamber.services.partneruser.PartnerUserUpdated
import StateUtils._


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


    protected def handle = transactional {
        case msg @ ReadForEmail(email) =>
            val eu = from(echoedUsers)(eu => where(eu.email === email) select(eu)).headOption
            val eus = readEchoedUserSettings(eu)
            val fu = eu.map(_.facebookUserId).flatMap(facebookUsers.lookup(_))
            val tu = eu.map(_.twitterUserId).flatMap(twitterUsers.lookup(_))
            val nf = readNotifications(eu)

            eu.cata(
                _ => sender ! ReadForEmailResponse(msg, Right(EchoedUserServiceState(eu.get, eus.get, fu, tu, nf))),
                sender ! ReadForEmailResponse(msg, Left(EchoedUserNotFound(email))))


        case msg @ ReadForCredentials(credentials) =>
            echoedUsers.lookup(credentials.id).foreach { eu =>
                val eus = readEchoedUserSettings(Option(eu)).get
                val fu = Option(eu.facebookUserId).flatMap(facebookUsers.lookup(_))
                val tu = Option(eu.twitterUserId).flatMap(twitterUsers.lookup(_))
                val nf = readNotifications(Option(eu))

                context.sender ! ReadForCredentialsResponse(msg, Right(EchoedUserServiceState(eu, eus, fu, tu, nf)))
            }


        case msg @ ReadForFacebookUser(facebookUser) =>
            val fu = from(facebookUsers)(fu => where(fu.facebookId === facebookUser.facebookId) select(fu)).headOption
            val eu = fu.map(_.echoedUserId).flatMap(echoedUsers.lookup(_))
            val eus = readEchoedUserSettings(eu)
            val tu = eu.map(_.twitterUserId).flatMap(twitterUsers.lookup(_))
            val nf = readNotifications(eu)

            fu.cata(
                _ => context.sender ! ReadForFacebookUserResponse(msg, Right(EchoedUserServiceState(eu.get, eus.get, fu, tu, nf))),
                context.sender ! ReadForFacebookUserResponse(msg, Left(FacebookUserNotFound(facebookUser))))


        case msg @ ReadForTwitterUser(twitterUser) =>
            val tu = from(twitterUsers)(tu => where(tu.twitterId === twitterUser.twitterId) select(tu)).headOption
            val eu = tu.map(_.echoedUserId).flatMap(echoedUsers.lookup(_))
            val eus = readEchoedUserSettings(eu)
            val fu = eu.map(_.facebookUserId).flatMap(facebookUsers.lookup(_))
            val nf = readNotifications(eu)

            tu.cata(
                _ => context.sender ! ReadForTwitterUserResponse(msg, Right(EchoedUserServiceState(eu.get, eus.get, fu, tu, nf))),
                context.sender ! ReadForTwitterUserResponse(msg, Left(TwitterUserNotFound(twitterUser))))


        case EchoedUserCreated(echoedUser, eus, facebookUser, twitterUser) =>
            echoedUsers.insert(echoedUser)
            echoedUserSettings.insert(EchoedUserSettings(eus))
            facebookUsers.insert(facebookUser)
            twitterUsers. insert(twitterUser)


        case EchoedUserUpdated(echoedUser, eus, facebookUser, twitterUser) =>
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


        case NotificationCreated(notification) => notifications.insert(Notification(notification))

        case NotificationUpdated(notification) => notifications.update(Notification(notification.copy(updatedOn = new Date)))


        case msg: ReadSchedulerServiceState =>
            sender ! ReadSchedulerServiceStateResponse(
                    msg,
                    Right(from(schedules)(s => select(s)).map(s => (s.id, s.convertTo)).toMap))


        case ScheduleCreated(schedule) => schedules.insert(Schedule(schedule))

        case ScheduleDeleted(schedule) => schedules.delete(from(schedules)(s => where(s.id === schedule.id) select(s)))


        case msg @ ReadPartnerUserForEmail(email) =>
            from(partnerUsers)(pu => where(pu.email === email) select(pu)).headOption.cata(
                pu => sender ! ReadPartnerUserForEmailResponse(msg, Right(pu)),
                sender ! ReadPartnerUserForEmailResponse(msg, Left(PartnerUserNotFound(email))))


        case msg @ ReadPartnerUserForCredentials(credentials) =>
            partnerUsers.lookup(credentials.partnerUserId).foreach { pu =>
                sender ! ReadPartnerUserForCredentialsResponse(msg, Right(pu))
            }


        case msg @ ReadAdminUserForEmail(email) =>
            from(adminUsers)(au => where(au.email === email) select(au)).headOption.cata(
                au => sender ! ReadAdminUserForEmailResponse(msg, Right(au)),
                sender ! ReadAdminUserForEmailResponse(msg, Left(AdminUserNotFound(email))))


        case msg @ ReadAdminUserForCredentials(credentials) =>
            adminUsers.lookup(credentials.adminUserId).foreach { au =>
                sender ! ReadAdminUserForCredentialsResponse(msg, Right(au))
            }


        case PartnerUserUpdated(partnerUser) => partnerUsers.update(partnerUser.copy(updatedOn = new Date))


        case msg @ ReadStory(id) =>
            stories.lookup(id).map(readStory(_)).foreach(s => sender ! ReadStoryResponse(msg, Right(s)))

        case msg @ ReadStoryForEcho(echoId, echoedUserId) =>
            from(echoes)(e => where(e.id === echoId and e.echoedUserId === echoedUserId) select(e)).headOption.map { e =>
                from(stories)(s => where(s.echoId === e.id) select(s)).map { s =>
                    readStory(s, Option(e))
                }.headOption.cata(
                    s => sender ! ReadStoryForEchoResponse(msg, Right(s)),
                    {
                        val eu = echoedUsers.lookup(e.echoedUserId).get
                        val img = images.lookup(e.imageId).get.convertTo
                        val p = partners.lookup(e.partnerId).get
                        val ps = partnerSettings.lookup(e.partnerSettingsId).get

                        sender ! ReadStoryForEchoResponse(msg, Left(StoryForEchoNotFound(
                                new StoryState(eu, p, ps, Option(e.convertTo(img)), Option(img)))))
                    })
            }

        case msg @ StoryCreated(storyState) => stories.insert(domain.Story(storyState))
        case msg @ StoryUpdated(storyState) => stories.update(domain.Story(storyState))
        case msg @ StoryTagged(storyState, _, _) => stories.update(domain.Story(storyState))
        case msg @ ChapterCreated(_, c, ci) =>
            chapters.insert(c)
            ci.map(chapterImages.insert(_))

        case msg @ ChapterUpdated(_, c, ci) =>
            chapters.update(c)
            chapterImages.deleteWhere(ci => ci.chapterId === c.id)
            ci.map(chapterImages.insert(_))

        case msg @ CommentCreated(_, c) => comments.insert(c)
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