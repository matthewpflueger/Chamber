package com.echoed.chamber.services.state

import com.echoed.chamber.services.{MessageResponse => MR, Correlated, EchoedException, Message}
import com.echoed.chamber.domain.StoryState
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials
import com.echoed.chamber.services.partneruser.{PartnerUserClientCredentials => PUCC}
import com.echoed.chamber.services.adminuser.{AdminUserClientCredentials => AUCC}
import com.echoed.chamber.domain.partner.{PartnerUser, Partner}
import org.springframework.validation.Errors
import akka.actor.ActorRef

sealed trait QueryMessage extends Message
sealed case class QueryException(message: String = "", cause: Throwable = null)
        extends EchoedException(message, cause)

import com.echoed.chamber.services.state.{QueryMessage => QM}
import com.echoed.chamber.services.state.{QueryException => QE}


case class FindAllStories(page: Int = 0, pageSize: Int = 30) extends QM
case class FindAllStoriesResponse(
                message: FindAllStories,
                value: Either[QE, List[StoryState]])
                extends QM with MR[List[StoryState], FindAllStories, QE]

case class QueryStoriesForAdmin(
        aucc: AdminUserClientCredentials,
        page: Int = 0,
        pageSize: Int = 30,
        moderated: Option[Boolean] = None) extends QM

case class QueryStoriesForAdminResponse(
                message: QueryStoriesForAdmin,
                value: Either[QE, List[StoryState]])
                extends QM with MR[List[StoryState], QueryStoriesForAdmin, QE]


case class QueryStoriesForPartner(
        pucc: PUCC,
        page: Int = 0,
        pageSize: Int = 30,
        moderated: Option[Boolean] = None) extends QM
case class QueryStoriesForPartnerResponse(
                message: QueryStoriesForPartner,
                value: Either[QE, List[StoryState]])
                extends QM with MR[List[StoryState], QueryStoriesForPartner, QE]


case class QueryPartners(aucc: AUCC, page: Int = 0, pageSize: Int = 30) extends QM
case class QueryPartnersResponse(message: QueryPartners, value: Either[QE, List[Partner]])
        extends QM with MR[List[Partner], QueryPartners, QE]

case class QueryPartnerUsers(aucc: AUCC, partnerId: String, page: Int = 0, pageSize: Int = 30) extends QM
case class QueryPartnerUsersResponse(message: QueryPartnerUsers, value: Either[QE, List[PartnerUser]])
        extends QM with MR[List[PartnerUser], QueryPartnerUsers, QE]


case class PartnerAndPartnerUsers(partner: Partner, partnerUser: PartnerUser)
case class QueryPartnersAndPartnerUsers(aucc: AUCC, page: Int = 0, pageSize: Int = 30) extends QM
case class QueryPartnersAndPartnerUsersResponse(
            message: QueryPartnersAndPartnerUsers,
            value: Either[QE, List[PartnerAndPartnerUsers]])
            extends QM with MR[List[PartnerAndPartnerUsers], QueryPartnersAndPartnerUsers, QE]



case class QueryUnique(ref: Any, correlation: Message, override val correlationSender: Option[ActorRef])
        extends QM with Correlated[Message]
case class QueryUniqueResponse(message: QueryUnique, value: Either[EchoedException, Boolean])
        extends QM with MR[Boolean, QueryUnique, EchoedException]

