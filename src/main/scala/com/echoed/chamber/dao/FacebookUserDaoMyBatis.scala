package com.echoed.chamber.dao

import com.echoed.chamber.domain.FacebookUser
import reflect.BeanProperty
import org.apache.ibatis.session.SqlSession


class FacebookUserDaoMyBatis extends FacebookUserDao {
    @BeanProperty var sqlSession: SqlSession = null

    def selectFacebookUserWithId(id: String): FacebookUser = {
        sqlSession.selectOne("selectFacebookUser", id).asInstanceOf[FacebookUser]
    }

    def insertOrUpdateFacebookUser(facebookUser: FacebookUser): FacebookUser = {
        sqlSession.insert("insertOrUpdateFacebookUser", facebookUser)
        facebookUser
    }

    def insertFacebookUser(facebookUser: FacebookUser): FacebookUser = {
        sqlSession.insert("insertFacebookUser", facebookUser)
        facebookUser
    }

    def updateFacebookUser(facebookUser: FacebookUser): FacebookUser = {
        sqlSession.update("updateFacebookUser", facebookUser)
        facebookUser
    }

}