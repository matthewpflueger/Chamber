package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class Retailer(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String) {


    def this(name: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        name)

}

