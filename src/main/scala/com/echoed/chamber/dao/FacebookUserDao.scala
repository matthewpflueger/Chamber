package com.echoed.chamber.dao

import com.echoed.chamber.domain.FacebookUser


trait FacebookUserDao {
    def selectFacebookUserWithId(id: String): FacebookUser

    def insertOrUpdateFacebookUser(facebookUser: FacebookUser): FacebookUser

    def insertFacebookUser(facebookUser: FacebookUser): FacebookUser

    def updateFacebookUser(facebookUser: FacebookUser): FacebookUser
}