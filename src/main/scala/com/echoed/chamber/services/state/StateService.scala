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
import com.echoed.chamber.services.state.schema._
import scala.Left
import com.echoed.chamber.services.echoeduser.EchoedUserUpdated
import scala.Right
import com.echoed.chamber.services.echoeduser.EchoedUserCreated
import com.echoed.chamber.services.echoeduser.NotificationCreated
import com.echoed.chamber.domain
import com.echoed.chamber.domain.{TwitterUser, FacebookUser, StoryState, EchoedUser}
import scala.collection.immutable.Stack
import com.echoed.chamber.services.scheduler.{ScheduleDeleted, ScheduleCreated}
import com.echoed.util.TransactionUtils._
import com.echoed.chamber.services.partneruser.PartnerUserUpdated
import StateUtils._
import com.echoed.util.{DateUtils, UUID}
import com.echoed.chamber.services.partner.PartnerServiceState


class StateService(
        val ep: EventProcessorActorSystem,
        val dataSource: DataSource) extends EchoedService with SquerylSessionFactory {

    private def readNotifications(eu: Option[EchoedUser]) =
        eu
            .map(eu => (from(notifications)(n =>
                    where(n.echoedUserId === eu.id)
                    select(n)
                    orderBy(n.createdOn asc))).toList)
//            .map(_.map(_.convertTo(eu.get)))
            .map(_.map(n => try { n.convertTo(eu.get) } catch { case e => log.error(e, "%s" format n); new com.echoed.chamber.domain.Notification() }))
            .filter(_.id != "")
            .map(Stack[domain.Notification]().pushAll(_))
            .getOrElse(Stack[domain.Notification]())

    private def readEchoedUserSettings(eu: Option[EchoedUser]) = eu.map { eu =>
            (from(echoedUserSettings)(eus => where(eus.echoedUserId === eu.id) select(eus))).single.convertTo(eu)
    }

    private def readFollowingUsers(eu: Option[EchoedUser]) =
        eu
            .map(eu => (from(followers)(f => where(f.ref === "EchoedUser" and f.echoedUserId === eu.id) select(f))).toList)
            .map(_.map(f => echoedUsers.lookup(f.refId).map(f.convertTo(_))).filter(_.isDefined).map(_.get))
            .getOrElse(List[echoeduser.Follower]())

    private def readFollowedByUsers(eu: Option[EchoedUser]) =
        eu
            .map(eu => (from(followers)(f => where(f.ref === "EchoedUser" and f.refId === eu.id) select(f))).toList)
            .map(_.map(f => echoedUsers.lookup(f.echoedUserId).map(f.convertTo(_))).filter(_.isDefined).map(_.get))
            .getOrElse(List[echoeduser.Follower]())

    private def readFollowingPartners(eu: Option[EchoedUser]) =
        eu
            .map(eu => (from(followers)(f => where(f.ref === "Partner" and f.echoedUserId === eu.id) select(f))).toList)
            .map(_.map(f => partners.lookup(f.refId).map(f.convertToPartnerFollower(_))).filter(_.isDefined).map(_.get))
            .getOrElse(List[echoeduser.PartnerFollower]())


    private def readEchoedUser(
            credentials: Option[EchoedUserClientCredentials] = None,
            facebookUser: Option[FacebookUser] = None,
            twitterUser: Option[TwitterUser] = None) = {
        val (eu, fu, tu) = ((credentials, facebookUser, twitterUser): @unchecked) match {
            case (Some(eucc), None, None) =>
                val eu = from(echoedUsers)(eu =>
                    where(eu.id === eucc.id or eu.email === eucc.id or eu.screenName === eucc.id)
                    select(eu)).headOption
                val fu = eu.map(_.facebookUserId).flatMap(facebookUsers.lookup(_))
                val tu = eu.map(_.twitterUserId).flatMap(twitterUsers.lookup(_))
                (eu, fu, tu)

            case (None, Some(facebookUser), None) =>
                val fu = from(facebookUsers)(fu => where(fu.facebookId === facebookUser.facebookId) select(fu)).headOption
                val eu = fu.map(_.echoedUserId).flatMap(echoedUsers.lookup(_))
                val tu = eu.map(_.twitterUserId).flatMap(twitterUsers.lookup(_))
                (eu, fu, tu)

            case (None, None, Some(twitterUser)) =>
                val tu = from(twitterUsers)(tu => where(tu.twitterId === twitterUser.twitterId) select(tu)).headOption
                val eu = tu.map(_.echoedUserId).flatMap(echoedUsers.lookup(_))
                val fu = eu.map(_.facebookUserId).flatMap(facebookUsers.lookup(_))
                (eu, fu, tu)
        }

        val eus = readEchoedUserSettings(eu)
        val nf = readNotifications(eu)
        val fus = readFollowingUsers(eu)
        val fbu = readFollowedByUsers(eu)
        val fp = readFollowingPartners(eu)

        eu.map(EchoedUserServiceState(_, eus.get, fu, tu, nf, fus, fbu, fp))
    }

    override def preStart() {
        super.preStart()
        ep.subscribe(self, classOf[Event])
//        ep.subscribe(self, classOf[CreatedEvent])
//        ep.subscribe(self, classOf[UpdatedEvent])
//        ep.subscribe(self, classOf[DeletedEvent])
    }

    protected def handle = transactional {
        case msg @ ReadForCredentials(c) =>
            readEchoedUser(Option(c)).cata(
                s => sender ! ReadForCredentialsResponse(msg, Right(s)),
                sender ! ReadForCredentialsResponse(msg, Left(EchoedUserNotFound(c.id))))


        case msg @ ReadForFacebookUser(facebookUser) =>
            readEchoedUser(facebookUser = Option(facebookUser)).cata(
                s => sender ! ReadForFacebookUserResponse(msg, Right(s)),
                sender ! ReadForFacebookUserResponse(msg, Left(FacebookUserNotFound(facebookUser))))


        case msg @ ReadForTwitterUser(twitterUser) =>
            readEchoedUser(twitterUser = Option(twitterUser)).cata(
                s => sender ! ReadForTwitterUserResponse(msg, Right(s)),
                sender ! ReadForTwitterUserResponse(msg, Left(TwitterUserNotFound(twitterUser))))


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


        case PartnerFollowerCreated(echoedUserId, follower) =>
            followers.insert(schema.Follower(UUID(), new Date, new Date, "Partner", follower.partnerId, echoedUserId))

        case FollowerCreated(echoedUserId, follower) =>
            followers.insert(schema.Follower(UUID(), new Date, new Date, "EchoedUser", follower.echoedUserId, echoedUserId))

        case FollowerDeleted(echoedUserId, follower) =>
            followers.deleteWhere(f => f.ref === "EchoedUser" and f.refId === follower.echoedUserId and f.echoedUserId === echoedUserId)


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

        case StoryModerated(_, moderation) => moderations.insert(moderation)
        case StoryCreated(storyState) => stories.insert(storyState.asStory)
        case StoryUpdated(storyState) => stories.update(storyState.asStory)
        case StoryTagged(storyState, _, _) => stories.update(storyState.asStory)

        case VoteUpdated(storyState, vote) =>
            votes.update(vote)
            stories.update(storyState.asStory)

        case VoteCreated(storyState, vote) =>
            votes.insert(vote)
            stories.update(storyState.asStory)

        case ChapterCreated(_, c, ci) =>
            chapters.insert(c)
            ci.map(chapterImages.insert(_))

        case ChapterUpdated(_, c, ci) =>
            chapters.update(c)
            chapterImages.deleteWhere(ci => ci.chapterId === c.id)
            ci.map(chapterImages.insert(_))

        case CommentCreated(storyState, c) =>
            comments.insert(c)
            stories.update(storyState.asStory)

        case StoryImageCreated(image) => images.insert(Image(image))

        case msg @ ReadPartner(pcc) =>
            val activeOn = DateUtils.dateToLong(new Date())

            val p = from(partners)(p => where(p.id === pcc.partnerId or p.handle === pcc.partnerId) select(p)).single
            val ps = from(partnerSettings)(ps =>
                    where((ps.partnerId === p.id) and (ps.activeOn lte activeOn))
                    select(ps)
                    orderBy(ps.activeOn desc)).page(0,1).toList.head
            val pu = from(partnerUsers)(pu => where(pu.partnerId === p.id) select(pu)).toList.headOption

            val fbus = (from(followers)(f => where(f.ref === "Partner" and f.refId === p.id) select(f))).toList
                    .map(f => from(echoedUsers)(eu =>
                        where(eu.id === f.echoedUserId)
                        select(eu.id, eu.name, eu.screenName, eu.facebookId, eu.twitterId)).single)
                    .map { case (i, n, s, f, t) => echoeduser.Follower(i, n, s, f, t) }

            sender ! ReadPartnerResponse(msg, Right(PartnerServiceState(p, ps, pu, fbus)))
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