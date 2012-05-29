package com.echoed.chamber

import org.springframework.context.support.FileSystemXmlApplicationContext
import org.slf4j.LoggerFactory


object Main {

    private val logger = LoggerFactory.getLogger(classOf[Main])

    def main(args: Array[String]) {
        logger.debug("Loading Spring context {}", args(0))
        val ctx = new FileSystemXmlApplicationContext(args(0))
        ctx.registerShutdownHook
    }

}

class Main {}
