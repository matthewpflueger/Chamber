package com.echoed.util

import java.util.Date
import java.text.SimpleDateFormat

object DateUtils {

    implicit def dateToLong(date: Date) =
        java.lang.Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(date))

    implicit def longToDate(long: Long) =
        new SimpleDateFormat("yyyyMMddHHmmss").parse(long.toString)
}
