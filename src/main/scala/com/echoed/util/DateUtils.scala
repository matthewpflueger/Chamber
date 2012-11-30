package com.echoed.util

import java.util.Date
import java.text.{ParseException, SimpleDateFormat}
import java.lang.Long.parseLong
import scala.util.control.Exception._

object DateUtils {

    private def failWithNone = failAsValue(classOf[NumberFormatException], classOf[ParseException])(None)

    private def parseToLong(date: Date) = parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(date))
    private def parseToDate(long: Long) = {
        val f = new SimpleDateFormat("yyyyMMddHHmmss")
        f.setLenient(false)
        f.parse(long.toString)
    }

    implicit def dateToLong(date: Date) = parseToLong(date)
    implicit def longToDate(long: Long) = parseToDate(long)

    implicit def dateToOptionLong(date: Date) = failWithNone { Option(parseToLong(date)) }
    implicit def longToOptionDate(long: Long) = failWithNone { Option(parseToDate(long)) }

    implicit def optionDateToOptionLong(date: Option[Date]) = failWithNone { date.map(parseToLong(_)) }
    implicit def optionLongToOptionDate(long: Option[Long]) = failWithNone { long.map(parseToDate(_)) }

    def maxDate = 20371231120000L
}



