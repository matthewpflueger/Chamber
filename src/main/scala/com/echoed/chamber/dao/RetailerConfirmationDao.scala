package com.echoed.chamber.dao

import com.echoed.chamber.domain.RetailerConfirmation


trait RetailerConfirmationDao {

    def insertRetailerConfirmation(retailerConfirmation: RetailerConfirmation): RetailerConfirmation

    def selectRetailerConfirmationCount: Long

}