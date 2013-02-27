package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.views.content.ContentDescription

trait Context{
    def id:             String
    def title:          String
    def contextType:    String
    def contentType:    ContentDescription
    def content:        List[Map[String, Any]]
    def stats:          List[Map[String, Any]]
    def highlights:     List[Map[String, Any]]
}
