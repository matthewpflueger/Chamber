package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.DateUtils._
import com.echoed.util.UUID


case class Moderation(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        ref: String,
        refId: String,
        moderated: Boolean,
        moderatedBy: String,
        moderatedRef: String,
        moderatedRefId: String) extends DomainObject {

    def this() = this("", 0L, 0L, "", "", true, "", "", "")

    def this(
            ref: String,
            refId: String,
            moderatedBy: String,
            moderatedRef: String,
            moderatedRefId: String,
            moderated: Boolean = true) = this(
        UUID(),
        new Date,
        new Date,
        ref,
        refId,
        moderated,
        moderatedBy,
        moderatedRef,
        moderatedRefId)
}


