package com.echoed.chamber.dao

import com.echoed.chamber.domain.FacebookUser


trait FacebookUserDao {
    def selectFacebookUserWithId(id: String): FacebookUser

    def insertOrUpdateFacebookUser(facebookUser: FacebookUser): Int
}