package com.echoed.chamber.services.state

import com.echoed.chamber.services.{MessageResponse => MR, EchoedException, Message}
import com.echoed.chamber.domain.{StoryState}
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials

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


case class QueryStoriesForPartner(
        pucc: PartnerUserClientCredentials,
        page: Int = 0,
        pageSize: Int = 30,
        moderated: Boolean = false) extends QM

case class QueryStoriesForPartnerResponse(
                message: QueryStoriesForPartner,
                value: Either[QE, List[StoryState]])
                extends QM with MR[List[StoryState], QueryStoriesForPartner, QE]