package com.echoed.chamber.services



trait MessageResponse[R, M <: Message, E <: EchoedException] extends Serializable {
    this: Message =>

    val message: M
    val value: Either[E, R]

    override val version = message.version
    override val id = message.id
    override val correlation = Option(message)

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
