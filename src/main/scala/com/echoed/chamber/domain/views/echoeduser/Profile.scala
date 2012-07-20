package com.echoed.chamber.domain.views.echoeduser

import com.echoed.chamber.domain.EchoedUser

case class Profile(
    echoedUser: EchoedUser,
    totalCredit: Float,
    totalVisits: Int)
