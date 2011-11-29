package com.echoed.chamber

import javax.servlet.{ServletContextListener, ServletContextEvent}
import akka.util.AkkaLoader
import akka.actor.DefaultBootableActorLoaderService

/**
  * This class can be added to web.xml mappings as a listener to for startup/shutdown of services
  *<web-app>
  * ...
  *  <listener>
  *    <listener-class>com.echoed.chamber..Initializer</listener-class>
  *  </listener>
  * ...
  *</web-app>
  */
class Initializer extends ServletContextListener {
   lazy val loader = new AkkaLoader
   def contextDestroyed(e: ServletContextEvent): Unit = loader.shutdown
   def contextInitialized(e: ServletContextEvent): Unit =
     loader.boot(true, new DefaultBootableActorLoaderService) // with BootableRemoteActorService)
}
