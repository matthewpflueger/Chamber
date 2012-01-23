package com.echoed.chamber.services.facebook

import akka.dispatch.Future


trait FacebookServiceCreator {

    def createFromCode(code: String, queryString: String): Future[CreateFromCodeResponse]

    def createFromId(facebookUserId: String): Future[CreateFromIdResponse]

}
