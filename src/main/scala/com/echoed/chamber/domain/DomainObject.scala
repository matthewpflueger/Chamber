package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.DateUtils._

trait DomainObject {

    def id: String
    def updatedOn: Long
    def createdOn: Long

    def updatedOnDate: Date = updatedOn
    def createdOnDate: Date = createdOn
}
