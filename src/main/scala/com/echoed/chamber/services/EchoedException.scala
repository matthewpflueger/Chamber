package com.echoed.chamber.services

import akka.AkkaException
import org.springframework.validation._


case class EchoedException(
        msg: String = "",
        cse: Throwable = null,
        cde: Option[String] = None,
        args: Option[Array[AnyRef]] = None,
        errs: Option[Errors] = None) extends AkkaException(msg, cse) {

    def asErrors = {
        errs.getOrElse {
            val bindingResult = new BeanPropertyBindingResult(this, BindingResult.MODEL_KEY_PREFIX + this.getClass.getName)
            bindingResult.addError(new ObjectError(
                    this.getClass.getSimpleName,
                    cde.map(Array[String](_)).getOrElse(Array[String]()),
                    args.getOrElse(Array[AnyRef]()),
                    msg))
            bindingResult
        }
    }
}














