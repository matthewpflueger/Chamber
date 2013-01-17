package com.echoed.chamber.domain.views.context

trait Context{
    def id:             String
    def title:          String
    def contextType:    String
    def content:        List[Map[String, Any]]
    def stats:          List[Map[String, Any]]
    def highlights:     List[Map[String, Any]]
}
