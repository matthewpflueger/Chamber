package com.echoed.util

import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallback

object TransactionUtils {

    implicit def functionToTransactionCallback[A](f: TransactionStatus => A): TransactionCallback[A] = {
        new TransactionCallback[A] {
            def doInTransaction(status: TransactionStatus) = {
                f(status)
            }
        }
    }

}
