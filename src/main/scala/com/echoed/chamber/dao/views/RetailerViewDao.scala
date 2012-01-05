package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.RetailerSocialSummary

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/5/12
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */

trait RetailerViewDao {

    def getSocialActivityByRetailerId(retailerId: String): RetailerSocialSummary

}