package com.echoed.util

import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallback
import org.squeryl.PrimitiveTypeMode._
import scala.language.implicitConversions

object TransactionUtils {

    def transactional(partial: PartialFunction[Any, Unit]) = new PartialFunction[Any, Unit] {
        def apply(x: Any) { inTransaction(partial.apply(x)) }
        def isDefinedAt(x: Any) = partial.isDefinedAt(x)
    }

    implicit def functionToTransactionCallback[A](f: TransactionStatus => A): TransactionCallback[A] = {
        new TransactionCallback[A] {
            def doInTransaction(status: TransactionStatus) = {
                f(status)
            }
        }
    }

}
