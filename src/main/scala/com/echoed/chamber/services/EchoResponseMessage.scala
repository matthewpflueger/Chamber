package com.echoed.chamber.services



case class EchoResponseMessage(
        echoRequestMessage: EchoRequestMessage,
        facebookPostResponseMessage: FacebookPostResponseMessage,
        twitterPostResponseMessage: TwitterPostResponseMessage,
        errorMessage: Option[ErrorMessage] = None) extends ResponseMessage(
            requestMessage = echoRequestMessage,
            errorMessage = errorMessage)  {

}
