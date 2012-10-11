package com.echoed.chamber.domain

import com.echoed.util.UUID
import java.util.Date
import com.echoed.util.DateUtils._


case class Vote(
    id: String,
    updatedOn: Long,
    createdOn: Long,
    ref: String,
    refId: String,
    echoedUserId: String,
    value: Int) extends DomainObject {

    def this(
            ref: String,
            refId: String,
            echoedUserId: String,
            value: Int) = this(
        UUID(),
        new Date,
        new Date,
        ref,
        refId,
        echoedUserId,
        value
    )

}

