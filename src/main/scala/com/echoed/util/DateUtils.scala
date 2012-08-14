package com.echoed.util

import java.util.Date
import java.text.{ParseException, SimpleDateFormat}
import java.lang.Long.parseLong

object DateUtils {

    implicit def dateToLong(date: Date) =
        try {
            parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(date))
        } catch {
            //FIXME Should return Option[Long] and in case of exception None but we need to move away
            //from MyBatis and update our schema to allow nulls for createdOn, updatedOn columns...
            case e: NumberFormatException => 0L
        }

    implicit def longToDate(long: Long) =
        try {
            val f = new SimpleDateFormat("yyyyMMddHHmmss")
            f.setLenient(false)
            Option(f.parse(long.toString))
        } catch {
            case e: ParseException => None
        }

    implicit def optionDateToOptionLong(date: Option[Date]) =
        try {
            date.map(d => parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(d)))
        } catch {
            case e: NumberFormatException => None
        }

    implicit def optionLongToOptionDate(long: Option[Long]) =
        try {
            long.map { l =>
                val f = new SimpleDateFormat("yyyyMMddHHmmss")
                f.setLenient(false);
                f.parse(l.toString)
            }
        } catch {
            case e: ParseException => None
        }

}
