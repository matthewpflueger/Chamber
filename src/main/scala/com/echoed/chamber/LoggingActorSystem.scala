package com.echoed.chamber

import com.echoed.util.Logging
import akka.actor.ActorSystem

class LoggingActorSystem(actorSystem: ActorSystem) {
    Logging.factory = clazz => new LoggingActorSystemAdapter(akka.event.Logging(actorSystem, clazz))
}

class LoggingActorSystemAdapter(log: akka.event.LoggingAdapter) extends Logging {
    def isErrorEnabled: Boolean = log.isErrorEnabled
    def isWarningEnabled: Boolean = log.isWarningEnabled
    def isInfoEnabled: Boolean = log.isInfoEnabled
    def isDebugEnabled: Boolean = log.isDebugEnabled

    def error(cause: Throwable, message: String) = log.error(cause, message)
    def error(cause: Throwable, template: String, arg1: Any) = log.error(cause, template, arg1)
    def error(cause: Throwable, template: String, arg1: Any, arg2: Any) = log.error(cause, template, arg1, arg2)
    def error(cause: Throwable, template: String, arg1: Any, arg2: Any, arg3: Any) = log.error(cause, template, arg1, arg2, arg3)
    def error(cause: Throwable, template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.error(cause, template, arg1, arg2, arg3, arg4)

    def error(message: String) = log.error(message)
    def error(template: String, arg1: Any) = log.error(template, arg1)
    def error(template: String, arg1: Any, arg2: Any) = log.error(template, arg1, arg2)
    def error(template: String, arg1: Any, arg2: Any, arg3: Any) = log.error(template, arg1, arg2, arg3)
    def error(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.error(template, arg1, arg2, arg3, arg4)

    def warn(message: String) = log.warning(message)
    def warn(template: String, arg1: Any) = log.warning(template, arg1)
    def warn(template: String, arg1: Any, arg2: Any) = log.warning(template, arg1, arg2)
    def warn(template: String, arg1: Any, arg2: Any, arg3: Any) = log.warning(template, arg1, arg2, arg3)
    def warn(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.warning(template, arg1, arg2, arg3, arg4)

    def info(message: String) = log.info(message)
    def info(template: String, arg1: Any) = log.info(template, arg1)
    def info(template: String, arg1: Any, arg2: Any) = log.info(template, arg1, arg2)
    def info(template: String, arg1: Any, arg2: Any, arg3: Any) = log.info(template, arg1, arg2, arg3)
    def info(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.info(template, arg1, arg2, arg3, arg4)

    def debug(message: String) = log.debug(message)
    def debug(template: String, arg1: Any) = log.debug(template, arg1)
    def debug(template: String, arg1: Any, arg2: Any) = log.debug(template, arg1, arg2)
    def debug(template: String, arg1: Any, arg2: Any, arg3: Any) = log.debug(template, arg1, arg2, arg3)
    def debug(template: String, arg1: Any, arg2: Any, arg3: Any, arg4: Any) = log.debug(template, arg1, arg2, arg3, arg4)
}

