package com.echoed.chamber.dao

import java.util.{Date, List => JList}
import org.apache.ibatis.annotations.Param
import com.echoed.chamber.domain.{FacebookLike, FacebookPost}


trait FacebookLikeDao {

    def findByFacebookPostId(facebookPostId: String): JList[FacebookLike]

    def insertOrUpdate(facebookLike: FacebookLike): Int

    def deleteByFacebookPostId(facebookPostId: String): Int

}
