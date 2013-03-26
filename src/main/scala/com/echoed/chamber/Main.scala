package com.echoed.chamber

import org.springframework.context.support.{GenericXmlApplicationContext, FileSystemXmlApplicationContext}
import org.slf4j.LoggerFactory
import org.springframework.web.context.support.XmlWebApplicationContext
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.springframework.web.servlet.DispatcherServlet
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.thread.ExecutorThreadPool
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import com.echoed.chamber.filters.{JsonpCallbackFilter, AccessControlHeadersFilter, CacheControlFilter, CrawlFilter}
import ch.qos.logback.access.jetty.RequestLogImpl
import org.eclipse.jetty.server.handler.{ContextHandler, ResourceHandler, RequestLogHandler, HandlerCollection}
import javax.servlet.{MultipartConfigElement, ServletConfig, DispatcherType, ServletContext}
import java.util.{Properties, Locale, EnumSet}
import org.springframework.web.context.{ContextLoaderListener, ConfigurableWebApplicationContext}


object Main {

    private val log = LoggerFactory.getLogger(classOf[Main])


    def bootJetty8 {
        val configContext = new GenericXmlApplicationContext("classpath:chamber-properties.xml")
        configContext.registerShutdownHook()

        val serverProps = configContext.getBean("serverProperties", classOf[Properties])

        val threadPool = new ExecutorThreadPool(
                serverProps.getProperty("executor.minThreads").toInt,
                serverProps.getProperty("executor.maxThreads").toInt,
                serverProps.getProperty("executor.keepAlive").toLong)

        val selectConnector = new SelectChannelConnector()
        selectConnector.setPort(serverProps.getProperty("port").toInt)

        val sslConnector = new SslSelectChannelConnector()
        sslConnector.setKeystore(serverProps.getProperty("ssl.keystore"))
        sslConnector.setKeyPassword(serverProps.getProperty("ssl.keyPassword"))
        sslConnector.setPort(serverProps.getProperty("ssl.port").toInt)

        val handlerCollection = new HandlerCollection()

        val requestLog = new RequestLogImpl()
        requestLog.setResource("/logback-access.xml")
        val requestLogHandler = new RequestLogHandler()
        requestLogHandler.setRequestLog(requestLog)

        if(serverProps.getProperty("envType") == "dev"){
            val resourceHandler: ResourceHandler = new ResourceHandler()
            resourceHandler.setDirectoriesListed(false)
            resourceHandler.setResourceBase(serverProps.getProperty("resourceBase"))

            val contextHandler: ContextHandler = new ContextHandler()
            contextHandler.setContextPath("/resources")
            contextHandler.setHandler(resourceHandler)
            handlerCollection.addHandler(contextHandler)
        }

        handlerCollection.addHandler(requestLogHandler)

        val servletContextHandler = new ServletContextHandler(
                handlerCollection,
                "/" + serverProps.getProperty("context"),
                false,
                false)





        servletContextHandler.addLifeCycleListener(new LifeCycle.Listener {
            def lifeCycleStarting(event: LifeCycle) {
                val sc = servletContextHandler.getServletContext

                val default = sc.addServlet("default", classOf[DefaultServlet])
                default.setLoadOnStartup(1)
                default.setAsyncSupported(true)
                default.setInitParameter("resourceBase", serverProps.getProperty("resourceBase"))
                default.setInitParameter("dirAllowed", "false")

                val dispatchTypes = EnumSet.allOf(classOf[DispatcherType])
                Array(
                        new CrawlFilter,
                        new CacheControlFilter,
                        new AccessControlHeadersFilter,
                        new JsonpCallbackFilter).foreach { f =>
                    sc.addFilter(f.getClass.getSimpleName, f).addMappingForUrlPatterns(dispatchTypes, false, "/*")
                }

                val appContext = new XmlWebApplicationContext()
                appContext.setConfigLocation(serverProps.getProperty("springframework.applicationContext"))
                appContext.registerShutdownHook
                sc.addListener(new ContextLoaderListener(appContext))

                val servletAppContext = new XmlWebApplicationContext()
                servletAppContext.setConfigLocation(serverProps.getProperty("springframework.servletContext"))
                servletAppContext.registerShutdownHook

                val dispatcher = sc.addServlet("dispatcher", new DispatcherServlet(servletAppContext))
                dispatcher.setMultipartConfig(new MultipartConfigElement("/tmp", 20971520, 41943040, 102400))
                dispatcher.setLoadOnStartup(1)
                dispatcher.setAsyncSupported(true)
                dispatcher.addMapping("/")
            }

            def lifeCycleStarted(event: LifeCycle) {}
            def lifeCycleFailure(event: LifeCycle, cause: Throwable) {}
            def lifeCycleStopping(event: LifeCycle) {}
            def lifeCycleStopped(event: LifeCycle) {}
        })

        val httpServer = new Server()
        httpServer.setThreadPool(threadPool)
        httpServer.setConnectors(Array(selectConnector, sslConnector))
        httpServer.setHandler(handlerCollection)
        httpServer.setStopAtShutdown(true)
        httpServer.start()
        httpServer.join()
    }


    def main(args: Array[String]) {
        bootJetty8
    }

}

class Main {}
