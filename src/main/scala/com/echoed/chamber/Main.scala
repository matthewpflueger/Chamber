package com.echoed.chamber

import org.springframework.context.support.FileSystemXmlApplicationContext


object Main {

    def main(args: Array[String]) {
        val ctx = new FileSystemXmlApplicationContext(args(0))
        ctx.registerShutdownHook
    }

}