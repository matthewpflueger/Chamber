package com.echoed.chamber.services.facebook

import com.echoed.chamber.domain.{FacebookTestUser, FacebookUser}
import scala.reflect.BeanProperty


class Me() {

    @BeanProperty var id: String = null
    @BeanProperty var name: String = null
    @BeanProperty var first_name: String = null
    @BeanProperty var middle_name: String = null
    @BeanProperty var last_name: String = null
    @BeanProperty var link: String = null
    @BeanProperty var gender: String = null
    @BeanProperty var email: String = null
    @BeanProperty var timezone: String = null
    @BeanProperty var locale: String = null
    @BeanProperty var verified: String = null
    @BeanProperty var updated_time: String = null

    def createFacebookUser(accessToken: String) = new FacebookUser(
        null,
        id,
        name,
        email,
        link,
        gender,
        timezone,
        locale,
        accessToken
    )

    def createFacebookTestUser(loginUrl: String, accessToken: String, password: String = "1234567890") = new FacebookTestUser(
        id,
        name,
        email,
        password,
        loginUrl,
        accessToken)
}
