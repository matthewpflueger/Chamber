package com.echoed.chamber.filters

import javax.servlet._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import com.gargoylesoftware.htmlunit.{BrowserVersion, WebClient}
import com.gargoylesoftware.htmlunit.html.HtmlPage
import scala.collection.mutable.MutableList
import java.net.URL
import scala.io.Source


class CrawlFilter extends Filter {

    private final val log = LoggerFactory.getLogger(classOf[CrawlFilter])

//    private final val client = new WebClient(BrowserVersion.FIREFOX_10)

    def init(filterConfig: FilterConfig) {}

    def doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val request = servletRequest.asInstanceOf[HttpServletRequest]
        val response = servletResponse.asInstanceOf[HttpServletResponse]
        val queryString = request.getQueryString

        if (queryString == null || !queryString.contains("_escaped_fragment_=")) {
            chain.doFilter(request, response)
        } else {
            val context = request.startAsync(request, response)
            //probably should be doing this in an external executor...
            context.start(new Runnable {
                def run() {
                    val time = System.currentTimeMillis()
                    //we are only handling urls like http://echoed.com/?_escaped_fragment_=<object>/<id>
                    //example: http://localhost.com:8080/?_escaped_fragment_=story/92d40761-a6bc-434f-b366-cf86bf627b28
                    val url = "%s%s" format(
                            request.getRequestURL,
                            queryString.replace("&_escaped_fragment_=", "#!").replace("_escaped_fragment_=", "#!"))

                    log.debug("Serving crawl {}", url)
                    var client: WebClient = null
                    try {
                        //CHROME_16 does not work but the default (IE 7) weirdly does...
                        client = new WebClient(BrowserVersion.FIREFOX_10)
                        client.setUseInsecureSSL(true)
                        client.setThrowExceptionOnFailingStatusCode(false)
                        client.setThrowExceptionOnScriptError(false)
                        val page: HtmlPage = client.getPage(url)
                        client.waitForBackgroundJavaScript(25000)
                        response.getOutputStream.println(page.asXml)
                    } catch {
                        case e => log.error("Error serving crawl %s" format url, e)
                    } finally {
                        Option(client).map(_.closeAllWindows())
                        context.complete()
                        log.debug("Completed in {} milliseconds crawl {}", System.currentTimeMillis() - time, url)
                    }
                }
            })
        }
    }

    def destroy() {}

}


object TestPool extends App {

    val num = 100
    val times = MutableList[Long]()
    times.sizeHint(num)
    for (i <- 1 to num) {
        val current = System.currentTimeMillis()
        val bytes = Source.fromInputStream(
            new URL("http://localhost.com:8080/?_escaped_fragment_=story/92d40761-a6bc-434f-b366-cf86bf627b28").openStream(),
            "UTF-8").length

        val time = System.currentTimeMillis() - current
        times += time
        println("Fetched %s in %s" format(bytes, time))
    }

    val total = times.reduce((t1, t2) => t1 + t2)
    println("Fetched url %s times in %s" format(times.size, total))
    val avg = total / times.size
    println("Average time %s" format avg)
    //Average time 5140 with pooled WebClient
    //Average time 5745 without pooled WebClient
    //Conclusion: not enough savings to pool at this level - should create/cache static content offline for crawlers

}