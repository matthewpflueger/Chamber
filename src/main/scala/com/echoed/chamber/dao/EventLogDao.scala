package com.echoed.chamber.dao


import com.echoed.chamber.domain.EventLog

trait EventLogDao {

    def insert(eventLog: EventLog): Int

}
