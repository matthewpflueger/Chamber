package com.echoed.chamber.services.partner

import akka.actor.PoisonPill
import com.echoed.chamber.domain.Notification
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.domain.partner.PartnerSettings
import com.echoed.chamber.domain.partner.PartnerUser
import com.echoed.chamber.services._
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.echoeduser.FollowPartner
import com.echoed.chamber.services.echoeduser.Follower
import com.echoed.chamber.services.echoeduser.RegisterNotification
import com.echoed.chamber.services.email.SendEmail
import com.echoed.chamber.services.state._
import com.echoed.util.{DateUtils, Encrypter}
import java.util.{Date, UUID}
import scala.Left
import scala.Right
import scala.Some


class PartnerService(
        mp: MessageProcessor,
        ep: EventProcessor,
        encrypter: Encrypter,
        initMessage: Message,
        accountManagerEmail: String = "accountmanager@echoed.com",
        accountManagerEmailTemplate: String = "partner_accountManager_email") extends OnlineOfflineService {

    protected var partner: Partner = _
    private var partnerSettings: PartnerSettings = _
    private var partnerUser: Option[PartnerUser] = None


    private var followedByUsers = List[Follower]()


    override def preStart() {
        super.preStart()
        initMessage match {
            case msg: PartnerIdentifiable => mp.tell(ReadPartner(msg.credentials), self)
            case msg: RegisterPartner => //handled in init
        }
    }

    private def becomeOnlineAndRegister {
        becomeOnline
        context.parent ! RegisterPartnerService(partner)
    }

    def init = {
        case msg @ RegisterPartner(userName, email, siteName, siteUrl, shortName, community) =>
            mp.tell(QueryUnique(msg, msg, Option(sender)), self)

        case QueryUniqueResponse(QueryUnique(_, msg: RegisterPartner, Some(channel)), Left(e)) =>
            channel ! RegisterPartnerResponse(msg, Left(InvalidRegistration(e.asErrors())))
            self ! PoisonPill

        case QueryUniqueResponse(QueryUnique(_, msg: RegisterPartner, Some(channel)), Right(true)) =>
            partner = new Partner(msg.siteName, msg.siteUrl, msg.shortName, msg.community).copy(secret = encrypter.generateSecretKey)
            partnerSettings = new PartnerSettings(partner.id, partner.handle)

            val password = UUID.randomUUID().toString
            partnerUser = Some(new PartnerUser(msg.userName, msg.email)
                    .copy(partnerId = partner.id)
                    .createPassword(password))

            val code = encrypter.encrypt(
                    """{ "password": "%s", "createdOn": "%s" }"""
                    format(password, DateUtils.dateToLong(new Date)))


            channel ! RegisterPartnerResponse(msg, Right(partnerUser.get, partner))

            ep(PartnerCreated(partner, partnerSettings, partnerUser.get))
            becomeOnlineAndRegister

            val model = Map(
                "code" -> code,
                "partner" -> partner,
                "partnerUser" -> partnerUser)

            mp(SendEmail(
                partnerUser.get.email,
                "Your Echoed Account",
                "partner_email_register",
                model))

            mp(SendEmail(
                accountManagerEmail,
                "New partner %s" format partner.name,
                accountManagerEmailTemplate,
                model))

        case msg @ ReadPartnerResponse(_, Right(pss)) =>
            partner = pss.partner
            partnerSettings = pss.partnerSettings
            partnerUser = pss.partnerUser
            followedByUsers = pss.followedByUsers
            becomeOnlineAndRegister
    }

    def online = {

        case QueryFollowersForPartnerResponse(_, Right(f)) => followedByUsers = followedByUsers ++ f

        case msg: FetchPartner => sender ! FetchPartnerResponse(msg, Right(partner))

        case msg: FetchPartnerAndPartnerSettings =>
            sender ! FetchPartnerAndPartnerSettingsResponse(
                    msg,
                    Right(new PartnerAndPartnerSettings(partner, partnerSettings)))

        case msg @ RequestStory(_) =>
            sender ! RequestStoryResponse(msg, Right(RequestStoryResponseEnvelope(partner, partnerSettings)))


        case msg @ NotifyPartnerFollowers(_, eucc, notification) =>
            var sendFollowRequest = true
            followedByUsers.foreach { f =>
                if (f.echoedUserId == eucc.id) sendFollowRequest = false
                else mp(RegisterNotification(
                        EchoedUserClientCredentials(f.echoedUserId),
                        new Notification(
                                f.echoedUserId,
                                partner,
                                notification.category,
                                notification.value,
                                Some("weekly"))))
            }
            if (sendFollowRequest) mp(FollowPartner(eucc, partner.id))


        case msg @ AddPartnerFollower(_, eu) if (!followedByUsers.exists(_.echoedUserId == eu.id)) =>
            sender ! AddPartnerFollowerResponse(msg, Right(partner))
            followedByUsers = Follower(eu) :: followedByUsers

    }

}





