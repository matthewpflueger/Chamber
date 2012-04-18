package com.echoed.chamber.controllers

trait NetworkController {

    def makeAuthorizeUrl(postAuthorizeUrl: String, add: Boolean = false, fullPermissions: Boolean = true): String

}


