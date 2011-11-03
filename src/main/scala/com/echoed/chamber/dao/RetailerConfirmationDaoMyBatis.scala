package com.echoed.chamber.dao

import org.apache.ibatis.session.SqlSession
import com.echoed.chamber.domain.RetailerConfirmation
import reflect.BeanProperty


class RetailerConfirmationDaoMyBatis extends RetailerConfirmationDao {

    @BeanProperty var sqlSession: SqlSession = null

    def insertRetailerConfirmation(retailerConfirmation: RetailerConfirmation) = {
        sqlSession.insert("insertEchoButtonData", retailerConfirmation)
        retailerConfirmation
    }
}