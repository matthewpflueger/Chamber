package com.echoed.chamber.domain

import com.echoed.util.UUID
import java.util.Date
import com.echoed.util.DateUtils._


case class Vote(
    id: String,
    updatedOn: Long,
    createdOn: Long,
    obj: String,
    objId: String,
    echoedUserId: String) extends DomainObject {

    def this(
            obj: String,
            objId: String,
            echoedUserId: String) = this(
        UUID(),
        new Date,
        new Date,
        obj,
        objId,
        echoedUserId
    )

}

