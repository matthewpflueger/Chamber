package com.echoed.chamber.services.state

import org.squeryl.PrimitiveTypeMode._
import com.echoed.chamber.services.state.schema.ChamberSchema._
import javax.sql.DataSource
import com.echoed.chamber.services.EchoedService
import com.echoed.util.TransactionUtils._
import com.echoed.chamber.services.partneruser.{PartnerUserClientCredentials => PUCC}
import StateUtils._


class QueryService(val dataSource: DataSource) extends EchoedService with SquerylSessionFactory {

    protected def handle = transactional {
        case msg @ FindAllStories(page, pageSize) =>
            val now = System.currentTimeMillis()
            sender ! FindAllStoriesResponse(msg, Right(from(stories)(s => select(s)).map(readStory(_)).toList))
            log.error("Querying all stories took %s" format System.currentTimeMillis() - now)
    }
}
