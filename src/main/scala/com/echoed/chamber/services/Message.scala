package com.echoed.chamber.services

import akka.actor.ActorRef
import scala.concurrent.Future


trait Message extends Serializable


trait Correlated[M <: Message] {
    def correlation: M
    def correlationSender: Option[ActorRef] = None
}

object ResponseValue {
    def apply[E, R](either: Either[E, R]) = new ResponseValue[E, R] { def value = either }
    def right[R](result: R) = new ResponseValue[EchoedException, R] { def value = Right(result) }
}

trait ResponseValue[E <: Any, R <: Any] {
    def value: Either[E, R]
}

trait MessageResponse[R, M <: Message, E <: EchoedException] extends ResponseValue[E, R] with Correlated[M] {
    this: Message =>

    val message: M

    override val correlation = message

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

trait MessageProcessor {
//    def apply(message: Message): Future[MessageResponse[_, _, _]]
    def apply(message: Message): Future[Any]
    def tell(message: Message, sender: ActorRef): Unit
}
