package com.echoed.chamber.dao

import com.echoed.chamber.domain.Retailer


trait RetailerDao {

    def findById(id: String): Retailer

    def insertOrUpdate(retailer: Retailer): Int

}