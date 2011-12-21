package com.echoed.chamber.dao

import java.util.{List => JList}
import com.echoed.chamber.domain.FacebookComment


trait FacebookCommentDao {

    def findByFacebookPostId(facebookPostId: String): JList[FacebookComment]

    def insertOrUpdate(facebookComment: FacebookComment): Int

    def deleteByFacebookPostId(facebookPostId: String): Int

}
