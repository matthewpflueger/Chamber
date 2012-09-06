package com.echoed.chamber.services.state

import javax.sql.DataSource
import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.MySQLAdapter
import com.echoed.chamber.services.EchoedService

private[state] trait SquerylSessionFactory { self: EchoedService =>

    def dataSource: DataSource

    SessionFactory.concreteFactory = Some(() => {
        val session = Session.create(dataSource.getConnection, new MySQLAdapter)
        session.setLogger(msg => log.debug(msg))
        session
    })

}
