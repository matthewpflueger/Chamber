package com.echoed.util.mustache

import com.github.mustachejava.reflect.ReflectionObjectHandler
import java.lang.reflect.{Field, Method}
import scala.collection.JavaConversions._
import scala.Some
import java.util.concurrent.Callable
import com.github.mustachejava.Iteration
import java.io.Writer
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class EchoedObjectHandler  extends ReflectionObjectHandler {

    // Allow any method or field
    override def checkMethod(member: Method) {}

    override def checkField(member: Field) {}

    override def coerce(value: Object) = {
        value match {
            case m: Map[_, _] => mapAsJavaMap(m)
            case o: Option[_] => (o: @unchecked) match {
                case Some(some: Object) => coerce(some)
                case None => null
            }
            case f: Future[_] => {
                new Callable[Any]() {
                    def call() = {
                        val value = Await.result(f, 20.seconds).asInstanceOf[Object]
                        coerce(value)
                    }
                }
            }
            case _ => value
        }
    }

    override def iterate(iteration: Iteration, writer: Writer, value: Object, scopes: Array[Object]) = {
        value match {
            case (t: Traversable[AnyRef] @unchecked) => {
                var newWriter = writer
                t map {
                    next =>
                        newWriter = iteration.next(newWriter, coerce(next), scopes)
                }
                newWriter
            }
            case n: Number => if (n == 0) writer else iteration.next(writer, coerce(value), scopes)
            case _ => super.iterate(iteration, writer, value, scopes)
        }
    }

    override def falsey(iteration: Iteration, writer: Writer, value: Object, scopes: Array[Object]) = {
        value match {
            case t: Traversable[_] => {
                if (t.isEmpty) {
                    iteration.next(writer, value, scopes)
                } else {
                    writer
                }
            }
            case n: Number => if (n == 0) iteration.next(writer, coerce(value), scopes) else writer
            case _ => super.falsey(iteration, writer, value, scopes)
        }
    }
}

