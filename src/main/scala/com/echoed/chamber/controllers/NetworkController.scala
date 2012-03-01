package com.echoed.chamber.controllers

trait NetworkController {

    def makeAuthorizeUrl(postAuthorizeUrl: String, add: Boolean = false): String

}


