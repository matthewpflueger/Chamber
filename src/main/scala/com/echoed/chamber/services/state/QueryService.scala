package com.echoed.chamber.services.state


import com.echoed.chamber.services.state.schema.ChamberSchema._
import javax.sql.DataSource
import com.echoed.chamber.services.{EchoedException, EchoedService}
import com.echoed.util.TransactionUtils._
import StateUtils._
import org.squeryl.PrimitiveTypeMode._
import com.echoed.chamber.domain.EchoedUser
import org.springframework.validation.{BindException, FieldError}


class QueryService(val dataSource: DataSource) extends EchoedService with SquerylSessionFactory {

    protected def handle = transactional {
        case msg @ FindAllStories(page, pageSize) =>
            val now = System.currentTimeMillis()
            from(stories)(s => select(s)).foreach(s => sender ! FindAllStoriesResponse(msg, Right(List(readStory(s)))))
//            sender ! FindAllStoriesResponse(msg, Right(from(stories)(s => select(s)).map(readStory(_)).toList))
            log.error("Querying all stories took %s" format System.currentTimeMillis() - now)

        case msg @ QueryStoriesForAdmin(aucc, page, pageSize, moderated) =>
            val ss = from(stories)(s =>
                        select(s)
                        orderBy(s.updatedOn desc))
                        .page(page * pageSize, pageSize)
                        .map(readStory(_))
                        .filter(moderated.isEmpty || _.isEchoedModerated == moderated.get)
                        .toList

            sender ! QueryStoriesForAdminResponse(msg, Right(ss))


        case msg @ QueryStoriesForPartner(pucc, page, pageSize, moderated) =>
            val ss = from(stories)(s =>
                        where(s.partnerId === pucc.partnerId.get)
                        select(s)
                        orderBy(s.updatedOn desc))
                        .page(page * pageSize, pageSize)
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

        case msg @ QueryPartners(aucc, page, pageSize) =>
            sender ! QueryPartnersResponse(msg, Right(from(partners)(p => select(p)).page(page, pageSize).toList))

        case msg @ QueryPartnerUsers(aucc, partnerId, page, pageSize) =>
            sender ! QueryPartnerUsersResponse(
                    msg,
                    Right(from(partnerUsers)(pu => where(pu.partnerId === partnerId) select(pu)).page(page, pageSize).toList))

        case msg @ QueryUnique(ref: EchoedUser, _, _) =>
            val results = List(
                    (from(echoedUsers)(eu => where(eu.id <> ref.id and eu.screenName === ref.screenName) compute(count)).single.measures,
                    "screenName",
                    "Screen name already taken"),
                    (from(echoedUsers)(eu => where(eu.id <> ref.id and eu.email === ref.email) compute(count)).single.measures,
                    "email",
                    "Email already taken"))
                .filter(_._1 > 0)
                .map(t3 => new FieldError("EchoedUser", t3._2, t3._3))

            if (results.isEmpty) sender ! QueryUniqueResponse(msg, Right(true))
            else {
                val be = new BindException(ref, "EchoedUser")
                results.foreach(be.addError(_))
                sender ! QueryUniqueResponse(msg, Left(EchoedException(msg = "Not unique", errs = Some(be))))//NotUniqueException().copy(errs = Some(be))))
            }
    }
}
