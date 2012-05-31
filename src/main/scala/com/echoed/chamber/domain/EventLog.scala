package com.echoed.chamber.domain

import java.util.{UUID, Date}


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
        UUID.randomUUID.toString,
        new Date,
        new Date,
        name,
        ref,
        refId)
}
