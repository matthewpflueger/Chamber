package com.echoed.chamber.services.state


import com.echoed.chamber.services.state.schema.ChamberSchema._
import javax.sql.DataSource
import com.echoed.chamber.services.EchoedService
import com.echoed.util.TransactionUtils._
import StateUtils._
import org.squeryl.PrimitiveTypeMode._


class QueryService(val dataSource: DataSource) extends EchoedService with SquerylSessionFactory {

    protected def handle = transactional {
        case msg @ FindAllStories(page, pageSize) =>
            val now = System.currentTimeMillis()
            from(stories)(s => select(s)).foreach(s => sender ! FindAllStoriesResponse(msg, Right(List(readStory(s)))))
//            sender ! FindAllStoriesResponse(msg, Right(from(stories)(s => select(s)).map(readStory(_)).toList))
            log.error("Querying all stories took %s" format System.currentTimeMillis() - now)

        case msg @ QueryStoriesForAdmin(aucc, page, pageSize, moderated) =>
            val ss = from(stories)(s => select(s) orderBy(s.updatedOn desc))
                        .map(readStory(_))
                        .filter(moderated.isEmpty || _.isEchoedModerated == moderated.get)
                        .toList

            sender ! QueryStoriesForAdminResponse(msg, Right(ss))


        case msg @ QueryStoriesForPartner(pucc, page, pageSize, moderated) =>
            val ss = from(stories)(s => where(s.partnerId === pucc.partnerId.get) select(s) orderBy(s.updatedOn desc))
                        .map(readStory(_))
                        .filter(moderated.isEmpty || _.isModerated == moderated.get)
                        .toList
//            val s = join(stories, moderations.leftOuter)((s, m) =>
//                where((s.partnerId === pucc.partnerId.get) and (m.get.refId isNull))
//                select(s, m)
//                on(s.id === m.get.refId)).toList
//            log.debug("Found {}", s)
//            val ss = s.map(tuple => readStory(tuple._1))

            sender ! QueryStoriesForPartnerResponse(msg, Right(ss))

    }
}
