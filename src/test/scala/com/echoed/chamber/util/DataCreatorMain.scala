package com.echoed.chamber.util

import org.springframework.context.support.{ClassPathXmlApplicationContext, FileSystemXmlApplicationContext}


object DataCreatorMain {

    def main(args: Array[String]) {
        val ctx = new ClassPathXmlApplicationContext("databaseIT.xml")
        ctx.registerShutdownHook
        ctx.refresh

        val dataCreator = ctx.getBean("dataCreator").asInstanceOf[DataCreator]
        //dataCreator.importFacebookTestUsers()
        //dataCreator.linkFacebookTestUsers()
//        dataCreator.addFacebookTestUsersToApps()
        dataCreator.removeFacebookTestUsersFromApps()
//        dataCreator.generateDataSet()

    }
}
