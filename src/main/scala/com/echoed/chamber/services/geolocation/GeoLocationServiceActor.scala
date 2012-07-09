package com.echoed.chamber.services.geolocation

import com.echoed.chamber.dao._
import akka.actor._

import scalaz._
import Scalaz._
import io.Source
import java.util.{UUID, Date}
import java.nio.charset.MalformedInputException
import akka.util.duration._
import akka.event.Logging
import java.net.SocketTimeoutException


class GeoLocationServiceActor(
        geoLocationDao: GeoLocationDao,
        geoLocationServiceUrl: String,
        lastUpdatedBeforeHours: Int = 72,
        findForCrawlIntervalMinutes: Int = 1) extends Actor {

    require(geoLocationServiceUrl != null, "geoLocationServiceUrl cannot be null")

    private final val logger = Logging(context.system, this)

    val lastUpdatedBeforeMillis: Long = lastUpdatedBeforeHours.toLong * 60 * 60 * 1000
    private var findClick = true


    override def preStart() {
        self ! FindForCrawl()
    }


    protected def receive = {

        case msg: FindForCrawl =>
            val me = context.self
            val lastUpdatedBefore = new Date
            lastUpdatedBefore.setTime(lastUpdatedBefore.getTime - lastUpdatedBeforeMillis)
            findClick = !findClick

            try {
                logger.debug("Looking for IP addresses to geo locate last located before {}", lastUpdatedBefore)
                Option(geoLocationDao.findForCrawl(lastUpdatedBefore, findClick))
                    .orElse {
                        findClick = !findClick
                        Option(geoLocationDao.findForCrawl(lastUpdatedBefore, findClick))
                    }.cata(
                        me ! GeoLocate(_),
                        context.system.scheduler.scheduleOnce(findForCrawlIntervalMinutes minutes, me, FindForCrawl()))
            } catch {
                case e =>
                    logger.error("Error finding ip to locate: {}", e)
                    context.system.scheduler.scheduleOnce(findForCrawlIntervalMinutes minutes, me, FindForCrawl())
            }

        case msg @ GeoLocate(gl) =>
            val channel = context.sender

            var geoLocation = gl.copy(
                        id = Option(gl.id).getOrElse(UUID.randomUUID().toString),
                        createdOn = Option(gl.createdOn).getOrElse(new Date))

            try {
                val url = geoLocationServiceUrl + gl.ipAddress
                logger.debug("Calling geolocation service: {}", url)
                val response = new String(Source.fromURL(url).toArray)
                //"66.202.133.170","66.202.133.170","US","United States","NY","New York","New York","","40.761900","-73.976300","Regus Business Center","Regus Business Center"
                logger.debug("Response from {}: {}", url, response)
                val split = response.split(",\"").map(_.replace("\"", ""))

                //sanity check
                require(split(0) == gl.ipAddress, "Response %s not for ip %s" format(response, gl.ipAddress))
                require(split.length == 12, "Response %s does not have 12 fields" format response)

                geoLocation = geoLocation.copy(
                    countryCode = split(2),
                    countryName = split(3),
                    regionCode = split(4),
                    regionName = split(5),
                    city = split(6),
                    postcode = split(7),
                    latitude = split(8),
                    longitude = split(9),
                    isp = split(10),
                    organization = split(11),
                    updateStatus = "crawled")

                logger.debug("Found {}", geoLocation)
                geoLocationDao.insertOrUpdate(geoLocation)
                channel ! GeoLocateResponse(msg, Right(geoLocation))

            } catch {
                case e: MalformedInputException =>
                    logger.debug("MalformedInputException occurred fetching IP address {}", gl.ipAddress)
                    channel ! GeoLocateResponse(msg, Left(MalformedResponse(gl.ipAddress, e)))
                    geoLocationDao.insertOrUpdate(geoLocation.copy(updateStatus = e.getMessage().take(254)))
                case e: IllegalArgumentException =>
                    logger.error("Error in response", e)
                    channel ! GeoLocateResponse(msg, Left(MalformedResponse(gl.ipAddress, e)))
                    geoLocationDao.insertOrUpdate(geoLocation.copy(updateStatus = e.getMessage().take(254)))
                case e: SocketTimeoutException =>
                    logger.debug("Timeout occurred fetching IP address {}", gl.ipAddress)
                    channel ! GeoLocateResponse(msg, Left(GeoLocationException(gl.ipAddress, e.getMessage, e)))
                    geoLocationDao.insertOrUpdate(geoLocation.copy(updateStatus = e.getMessage().take(254)))
                case e =>
                    logger.error("Unexpected error occurred", e)
                    channel ! GeoLocateResponse(msg, Left(GeoLocationException(gl.ipAddress, e.getMessage, e)))
                    geoLocationDao.insertOrUpdate(geoLocation.copy(updateStatus = e.getMessage().take(254)))
            }

        case msg: GeoLocateResponse =>
            self ! FindForCrawl()
    }
}




