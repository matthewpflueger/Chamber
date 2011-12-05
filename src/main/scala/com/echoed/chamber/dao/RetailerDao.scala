package com.echoed.chamber.dao

import com.echoed.chamber.domain.Retailer


trait RetailerDao {

    def findById(id: String): Retailer

    def insert(retailer: Retailer): Int

    def deleteById(id: String): Int

    def deleteByName(name: String): Int
}
