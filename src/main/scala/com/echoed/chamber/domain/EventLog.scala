package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID


case class EventLog(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        ref: String,
        refId: String) {

    def this(
            name: String,
            ref: String,
            refId: String) = this(
        UUID(),
        new Date,
        new Date,
        name,
        ref,
        refId)
}
