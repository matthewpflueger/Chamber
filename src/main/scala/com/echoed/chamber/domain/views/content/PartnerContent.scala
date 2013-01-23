package com.echoed.chamber.domain.views.content

import com.echoed.chamber.services.echoeduser.PartnerFollower

class PartnerContent( partner: PartnerFollower)  extends Content{

    val _type = "Partner"
    val _id = partner.partnerId
    val id = partner.partnerId

    def _title = null
    def _createdOn = 0
    def _updatedOn = 0
    def _views = 0
    def _votes = 0
    def _comments = 0

    def _plural = "Partner"
    def _singular = "Partners"
    def _endPoint = "partners"

}
