package com.echoed.chamber.services

import akka.AkkaException
import org.springframework.validation._
import java.util.{HashMap => JHashMap}


case class EchoedException(
        msg: String = "",
        cse: Throwable = null,
        code: Option[String] = None,
        arguments: Option[Array[AnyRef]] = None,
        errors: Option[Errors] = None) extends AkkaException(msg, cse) {

    def asErrors = {
        errors.getOrElse {
            val bindingResult = new BeanPropertyBindingResult(this, BindingResult.MODEL_KEY_PREFIX + this.getClass.getName)
            bindingResult.addError(new ObjectError(
                    this.getClass.getSimpleName,
                    code.map(Array[String](_)).getOrElse(Array[String]()),
                    arguments.getOrElse(Array[AnyRef]()),
                    msg))
            bindingResult
        }
    }
}














