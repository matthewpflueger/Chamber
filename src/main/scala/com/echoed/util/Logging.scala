package com.echoed.util

import java.util.Date
import org.slf4j.LoggerFactory


object Logging {
    var factory: Class[_] => Logging = clazz => new LoggingStdout(clazz)
//    var factory: Class[_] => Logging = clazz => new LoggingSlf4j(clazz)
    def apply(clazz: Class[_]) = factory(clazz)
}

trait Logging {

  def isErrorEnabled: Boolean
  def isWarningEnabled: Boolean
  def isInfoEnabled: Boolean
  def isDebugEnabled: Boolean

  def error(cause: Throwable, message: String)
  def error(cause: Throwable, template: String, arg1: Any)
  def error(cause: Throwable, template: String, arg1: Any, arg2: Any)
  def error(cause: Throwable, template: String, arg1: Any, arg2: Any, arg3: Any)
  def error(cause: Throwable, template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any)

  def error(message: String)
  def error(template: String, arg1: Any)
  def error(template: String, arg1: Any, arg2: Any)
  def error(template: String, arg1: Any, arg2: Any, arg3: Any)
  def error(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any)

  def warn(message: String)
  def warn(template: String, arg1: Any)
  def warn(template: String, arg1: Any, arg2: Any)
  def warn(template: String, arg1: Any, arg2: Any, arg3: Any)
  def warn(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any)

  def info(message: String)
  def info(template: String, arg1: Any)
  def info(template: String, arg1: Any, arg2: Any)
  def info(template: String, arg1: Any, arg2: Any, arg3: Any)
  def info(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any)

  def debug(message: String)
  def debug(template: String, arg1: Any)
  def debug(template: String, arg1: Any, arg2: Any)
  def debug(template: String, arg1: Any, arg2: Any, arg3: Any)
  def debug(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any)

}

class LoggingStdout(clazz: Class[_]) extends Logging {

    def isErrorEnabled: Boolean = true
    def isWarningEnabled: Boolean = true
    def isInfoEnabled: Boolean = true
    def isDebugEnabled: Boolean = true

    private def ft(level: String, template: String) =
            "" + new Date +
            " " + level.padTo(5, " ").mkString +
            " " + clazz.getSimpleName.take(40).padTo(40, " ").mkString +
            " " + template.replaceAllLiterally("{}", "%s")

    private def fte(template: String) = ft("ERROR", template)
    private def ftec(template: String) = fte(template) + "\n%s"
    private def ftw(template: String) = ft("WARN", template)
    private def fti(template: String) = ft("INFO", template)
    private def ftd(template: String) = ft("DEBUG", template)

    def error(cause: Throwable, message: String) = println(ftec(message) format(message, cause))
    def error(cause: Throwable, template: String, arg1: Any) = println(ftec(template) format(arg1, cause))
    def error(cause: Throwable, template: String, arg1: Any, arg2: Any) = println(ftec(template) format(arg1, arg2, cause))
    def error(cause: Throwable, template: String, arg1: Any, arg2: Any, arg3: Any) = println(ftec(template) format(arg1, arg2, arg3, cause))
    def error(cause: Throwable, template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = println(ftec(template) format(arg1, arg2, arg3, arg4, cause))


    def error(message: String) = println(fte(message))
    def error(template: String, arg1: Any) = println(fte(template) format(arg1))
    def error(template: String, arg1: Any, arg2: Any) = println(fte(template) format(arg1, arg2))
    def error(template: String, arg1: Any, arg2: Any, arg3: Any) = println(fte(template) format(arg1, arg2, arg3))
    def error(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = println(fte(template) format(arg1, arg2, arg3, arg4))

    def warn(message: String) = println(ftw(message))
    def warn(template: String, arg1: Any) = println(ftw(template) format(arg1))
    def warn(template: String, arg1: Any, arg2: Any) = println(ftw(template) format(arg1, arg2))
    def warn(template: String, arg1: Any, arg2: Any, arg3: Any) = println(ftw(template) format(arg1, arg2, arg3))
    def warn(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = println(ftw(template) format(arg1, arg2, arg3, arg4))

    def info(message: String) = println(fti(message))
    def info(template: String, arg1: Any) = println(fti(template) format(arg1))
    def info(template: String, arg1: Any, arg2: Any) = println(fti(template) format(arg1, arg2))
    def info(template: String, arg1: Any, arg2: Any, arg3: Any) = println(fti(template) format(arg1, arg2, arg3))
    def info(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = println(fti(template) format(arg1, arg2, arg3, arg4))

    def debug(message: String) = println(ftd(message))
    def debug(template: String, arg1: Any) = println(ftd(template) format(arg1))
    def debug(template: String, arg1: Any, arg2: Any) = println(ftd(template) format(arg1, arg2))
    def debug(template: String, arg1: Any, arg2: Any, arg3: Any) = println(ftd(template) format(arg1, arg2, arg3))
    def debug(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = println(ftd(template) format(arg1, arg2, arg3, arg4))
}

//class LoggingSlf4j(clazz: Class[_]) extends Logging {
//    private final val log = LoggerFactory.getLogger(clazz)
//
//    def isErrorEnabled: Boolean = log.isErrorEnabled
//    def isWarningEnabled: Boolean = log.isWarnEnabled
//    def isInfoEnabled: Boolean = log.isInfoEnabled
//    def isDebugEnabled: Boolean = log.isDebugEnabled
//
//    def error(cause: Throwable, message: String) = log.error(message, cause)
//    def error(cause: Throwable, template: String, arg1: Any) = log.error(cause, template, arg1)
//    def error(cause: Throwable, template: String, arg1: Any, arg2: Any) = log.error(cause, template, arg1, arg2)
//    def error(cause: Throwable, template: String, arg1: Any, arg2: Any, arg3: Any) = log.error(cause, template, arg1, arg2, arg3)
//    def error(cause: Throwable, template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.error(cause, template, arg1, arg2, arg3, arg4)
//
//    def error(message: String) = log.error(message)
//    def error(template: String, arg1: Any) = log.error(template, arg1)
//    def error(template: String, arg1: Any, arg2: Any) = log.error(template, arg1, arg2)
//    def error(template: String, arg1: Any, arg2: Any, arg3: Any) = log.error(template, arg1, arg2, arg3)
//    def error(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.error(template, arg1, arg2, arg3, arg4)
//
//    def warn(message: String) = log.warning(message)
//    def warn(template: String, arg1: Any) = log.warning(template, arg1)
//    def warn(template: String, arg1: Any, arg2: Any) = log.warning(template, arg1, arg2)
//    def warn(template: String, arg1: Any, arg2: Any, arg3: Any) = log.warning(template, arg1, arg2, arg3)
//    def warn(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.warning(template, arg1, arg2, arg3, arg4)
//
//    def info(message: String) = log.info(message)
//    def info(template: String, arg1: Any) = log.info(template, arg1)
//    def info(template: String, arg1: Any, arg2: Any) = log.info(template, arg1, arg2)
//    def info(template: String, arg1: Any, arg2: Any, arg3: Any) = log.info(template, arg1, arg2, arg3)
//    def info(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.info(template, arg1, arg2, arg3, arg4)
//
//    def debug(message: String) = log.debug(message)
//    def debug(template: String, arg1: Any) = log.debug(template, arg1)
//    def debug(template: String, arg1: Any, arg2: Any) = log.debug(template, arg1, arg2)
//    def debug(template: String, arg1: Any, arg2: Any, arg3: Any) = log.debug(template, arg1, arg2, arg3)
//    def debug(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.debug(template, arg1, arg2, arg3, arg4)
//}