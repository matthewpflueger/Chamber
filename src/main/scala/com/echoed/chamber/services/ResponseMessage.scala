package com.echoed.chamber.services


abstract case class ResponseMessage[R, M <: Message, E <: ErrorMessage](
        requestMessage: M,
        value: Either[E, R]) extends Message {


    override def messageVersion = requestMessage.messageVersion
    override def messageId = requestMessage.messageId
    override def messageCorrelation = Option(requestMessage)

    def resultOrException = value match {
        case Left(e)  ⇒ throw e
        case Right(r) => r
    }

    def foreach(f: R ⇒ Unit): Unit = value match {
        case Right(r) => f(r)
        case _ =>
    }

    def cata(fa: E => Unit, fb: R => Unit) = value match {
        case Left(a) => fa(a)
        case Right(b) => fb(b)
    }

    def fold[X](fa: E => X, fb: R => X) = value match {
        case Left(a) => fa(a)
        case Right(b) => fb(b)
    }
}
