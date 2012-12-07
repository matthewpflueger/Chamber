package com.echoed.chamber.services.state


import com.echoed.chamber.services.state.schema.ChamberSchema._
import javax.sql.DataSource
import com.echoed.chamber.services.{EchoedException, EchoedService}
import com.echoed.util.TransactionUtils._
import StateUtils._
import org.squeryl.PrimitiveTypeMode._
import com.echoed.chamber.domain.EchoedUser
import org.springframework.validation.{BindException, FieldError}
import com.echoed.chamber.services.echoeduser.Follower
import com.echoed.chamber.domain.partner.{Partner, PartnerUser}
import com.echoed.chamber.services.partner.RegisterPartner


class QueryService(val dataSource: DataSource) extends EchoedService with SquerylSessionFactory {

    protected def handle = transactional {
        case msg @ FindAllStories(page, pageSize) =>
            val now = System.currentTimeMillis()
            from(stories)(s => select(s)).foreach(s => sender ! FindAllStoriesResponse(msg, Right(List(readStory(s)))))
//            sender ! FindAllStoriesResponse(msg, Right(from(stories)(s => select(s)).map(readStory(_)).toList))
            log.error("Querying all stories took %s" format System.currentTimeMillis() - now)

        case msg @ FindAllTopics(page, pageSize) =>
            val results = from(topics)(t => select(t)).toList
            sender ! FindAllTopicsResponse(msg, Right(results))

        case msg @ QueryStoriesForAdmin(aucc, page, pageSize, moderated) =>
            val ss = from(stories)(s =>
                        select(s)
                        orderBy(s.updatedOn desc))
                        .page(page * pageSize, pageSize)
                        .map(readStory(_))
                        .filter(moderated.isEmpty || _.isEchoedModerated == moderated.get)
                        .toList

            sender ! QueryStoriesForAdminResponse(msg, Right(ss))

        case msg @ QueryEchoedUsersForAdmin(aucc, page, pageSize) =>
            val results = from(echoedUsers)(e =>
                            select(e)
                            orderBy(e.createdOn desc))
                            .page(page * pageSize, pageSize)
                            .toList

            sender ! QueryEchoedUsersForAdminResponse(msg, Right(results))

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

        case msg @ QueryPartnersAndPartnerUsers(aucc, page, pageSize) =>
            //Assumes only one partnerUser for each Partner
            val results = join(partners, partnerUsers)((p, pu) =>
                select(p, pu)
                on(p.id === pu.partnerId)).map( r => new PartnerAndPartnerUsers(r.a1, r.a2)).toList
            sender ! QueryPartnersAndPartnerUsersResponse(msg, Right(results))

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
                sender ! QueryUniqueResponse(msg, Left(EchoedException(msg = "Not unique", errs = Some(be))))
            }

        case msg @ QueryUnique(ref: RegisterPartner, _, _) =>
            val results = List(
                    (from(partnerUsers)(pu => where(pu.email === ref.email) compute(count)).single.measures,
                    "email",
                    "Email already taken"),
                    (from(partners)(p => where(p.name === ref.siteName) compute(count)).single.measures,
                    "siteName",
                    "Site name already taken"),
                    (from(partners)(p => where(p.handle === ref.shortName) compute(count)).single.measures,
                    "shortName",
                    "Shortname already taken"),
                    (from(partners)(p => where(p.domain === ref.siteUrl) compute(count)).single.measures,
                    "siteUrl",
                    "Site url already taken"))
                .filter(_._1 > 0)
                .map(t3 => new FieldError("RegisterPartner", t3._2, t3._3))

            if (results.isEmpty) sender ! QueryUniqueResponse(msg, Right(true))
            else {
                val be = new BindException(ref, "RegisterPartner")
                results.foreach(be.addError(_))
                sender ! QueryUniqueResponse(msg, Left(EchoedException(msg = "Not unique", errs = Some(be))))
            }

        case msg @ QueryFollowersForPartner(pcc) =>
            val results = (from(followers)(f => where(f.ref === "Partner" and f.refId === pcc.id) select(f))).toList
                    .map(f => from(echoedUsers)(eu =>
                        where(eu.id === f.echoedUserId)
                        select(eu.id, eu.name, eu.screenName, eu.facebookId, eu.twitterId)).single)
                    .map { case (i, n, s, f, t) => Follower(i, n, s, f, t) }

            sender ! QueryFollowersForPartnerResponse(msg, Right(results))


        case msg: QueryPartnerIds =>
            sender ! QueryPartnerIdsResponse(msg, Right(from(partners)(p => select(p.id)).toList))

        case msg @ QueryPartnerByIdOrHandle(idOrHandle) =>
            from(partners)(p => where(p.id === idOrHandle or p.handle === idOrHandle) select(p)).headOption.map { p =>
                sender ! QueryPartnerByIdOrHandleResponse(msg, Right(p))
            }

        case msg @ QueryEchoedUsersByFacebookId(_, ids) =>
            sender ! QueryEchoedUsersByFacebookIdResponse(msg, Right(
                    from(echoedUsers)(eu => where(eu.facebookId in ids) select(eu.id)).toList))

        case msg @ QueryEchoedUsersByTwitterId(_, ids) =>
            sender ! QueryEchoedUsersByTwitterIdResponse(msg, Right(
                    from(echoedUsers)(eu => where(eu.twitterId in ids) select(eu.id)).toList))
    }
}
