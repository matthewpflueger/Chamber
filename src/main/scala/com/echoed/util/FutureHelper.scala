package com.echoed.util

import akka.dispatch.Future
import org.slf4j.LoggerFactory


object FutureHelper {

    private final val logger = LoggerFactory.getLogger("com.echoed.util.FutureHelper")

    def get[A](future: () => Future[A]): Option[A] = {
        try {
            Option(future().get)
        } catch {
            case e =>
                logger.error("Error {}", e)
                None
        }
    }
}
