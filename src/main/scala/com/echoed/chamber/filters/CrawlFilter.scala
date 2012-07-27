package com.echoed.chamber.filters

import javax.servlet._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import com.gargoylesoftware.htmlunit.{BrowserVersion, WebClient}
import com.gargoylesoftware.htmlunit.html.HtmlPage


class CrawlFilter extends Filter {

    private final val log = LoggerFactory.getLogger(classOf[CrawlFilter])

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
                    try {
                        //CHROME_16 does not work but the default (IE 7) weirdly does...
                        val client = new WebClient(BrowserVersion.FIREFOX_10)
                        val page: HtmlPage = client.getPage(url)
                        client.waitForBackgroundJavaScript(20000)
                        response.getOutputStream.println(page.asXml)
                    } catch {
                        case e => log.error("Error serving crawl %s" format url, e)
                    } finally {
                        context.complete()
                        log.debug("Completed in {} milliseconds crawl {}", System.currentTimeMillis() - time, url)
                    }
                }
            })
        }
    }

    def destroy() {}

}
