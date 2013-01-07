package com.echoed.chamber.services

import akka.AkkaException
import org.springframework.validation._
import collection.JavaConversions._


class EchoedException(
        val msg: String = "",
        val cse: Throwable = null,
        val cde: Option[String] = None,
        val args: Option[Array[AnyRef]] = None,
        val errs: Option[Errors] = None) extends AkkaException(msg, cse) {

    def asErrors(objectName: Option[String] = None) =
        if (objectName.isDefined && errs.isDefined) {
            val on = objectName.get
            val be = new BindException(this, on)
            errs.get.getGlobalErrors.map(ge => be.addError(new ObjectError(on, ge.getDefaultMessage)))
            errs.get.getFieldErrors.map(fe => be.addError(new FieldError(on, fe.getField, fe.getDefaultMessage)))
            be
        } else errs.getOrElse {
            val be = new BindException(this, objectName.getOrElse(this.getClass.getSimpleName))
            be.addError(new ObjectError(
                    this.getClass.getSimpleName,
                    cde.map(Array[String](_)).getOrElse(Array[String]()),
                    args.getOrElse(Array[AnyRef]()),
                    msg))
            be
        }

}














