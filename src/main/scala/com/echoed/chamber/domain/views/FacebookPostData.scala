package com.echoed.chamber.domain.views

import scala.reflect.BeanProperty
import com.echoed.chamber.domain.{FacebookComment, FacebookLike, FacebookUser, FacebookPost}


case class FacebookPostData(
        id: String,
        facebookPost: FacebookPost,
        facebookUser: FacebookUser) {

    @BeanProperty var likes = List[FacebookLike]()
    @BeanProperty var comments = List[FacebookComment]()
}

