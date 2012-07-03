package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import scala.reflect.BeanProperty

import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.feed.{GetPartnerIdsResponse, GetStoryIdsResponse, FeedService}
import org.springframework.web.context.request.async.DeferredResult
import scala.collection.JavaConversions._


@Controller
class SitemapController {

    @BeanProperty var feedService: FeedService = _

    @RequestMapping(value = Array("/sitemap_index.xml"), method = Array(RequestMethod.GET))
    def sitemapindex = new ModelAndView("sitemap_index.xml")

    @RequestMapping(value = Array("/sitemap_pages.xml"), method = Array(RequestMethod.GET))
    def pages = new ModelAndView("sitemap_pages.xml")

    @RequestMapping(value = Array("/sitemap_partners.xml"), method = Array(RequestMethod.GET))
    def partners = {
        val result = new DeferredResult("error")

        feedService.getPartnerIds.onSuccess {
            case GetPartnerIdsResponse(_, Right(ids)) =>
                result.set(new ModelAndView("sitemap_partners.xml", Map("partners" -> ids)))
        }

        result
    }

    @RequestMapping(value = Array("/sitemap_stories.xml"), method = Array(RequestMethod.GET))
    def stories = {
        val result = new DeferredResult("error")

        feedService.getStoryIds.onSuccess {
            case GetStoryIdsResponse(_, Right(ids)) =>
                result.set(new ModelAndView("sitemap_stories.xml", Map("stories" -> ids)))
        }

        result
    }

}
