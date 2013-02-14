package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller


import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.feed._
import org.springframework.web.context.request.async.DeferredResult
import scala.collection.JavaConversions._
import com.echoed.chamber.services.feed.GetPartnerIdsResponse
import com.echoed.chamber.services.feed.GetPartnerIds
import com.echoed.chamber.services.feed.GetStoryIdsResponse
import scala.Right
import scala.concurrent.ExecutionContext.Implicits.global


@Controller
class SitemapController extends EchoedController {

    @RequestMapping(value = Array("/sitemap_index.xml"), method = Array(RequestMethod.GET))
    def sitemapindex = new ModelAndView("sitemap_index.xml")

    @RequestMapping(value = Array("/sitemap_pages.xml"), method = Array(RequestMethod.GET))
    def pages = new ModelAndView("sitemap_pages.xml")

    @RequestMapping(value = Array("/sitemap_partners.xml"), method = Array(RequestMethod.GET))
    def partners = {
        val result = new DeferredResult[ModelAndView](null, new ModelAndView("sitemap_partners.xml"))

        mp(GetPartnerIds()).onSuccess {
            case GetPartnerIdsResponse(_, Right(ids)) =>
                result.setResult(new ModelAndView("sitemap_partners.xml", Map("partners" -> ids)))
        }

        result
    }

    @RequestMapping(value = Array("/sitemap_stories.xml"), method = Array(RequestMethod.GET))
    def stories = {
        val result = new DeferredResult[ModelAndView](null, new ModelAndView("sitemap_stories.xml"))

        mp(GetStoryIds()).onSuccess {
            case GetStoryIdsResponse(_, Right(ids)) =>
                result.setResult(new ModelAndView("sitemap_stories.xml", Map("stories" -> ids)))
        }

        result
    }

}
