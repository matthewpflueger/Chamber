package com.echoed.util

import org.springframework.jdbc.datasource.{DataSourceUtils, DataSourceTransactionManager}
import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.MySQLAdapter

//ripped from http://lunajs.blogspot.com/2011/06/squeryl-with-java-experiment.html
class SquerylDataSourceTransactionManager extends DataSourceTransactionManager {

    def init() {
        SessionFactory.externalTransactionManagementAdapter = Some(() => {
            if (Session.hasCurrentSession) Session.currentSessionOption
            else {
                val s = new Session(DataSourceUtils.getConnection(getDataSource), new MySQLAdapter, None) {
                    override def cleanup = {
                        super.cleanup
                        unbindFromCurrentThread
                    }
                }

                s.bindToCurrentThread
                Some(s)
            }
        })
    }

    override def doCleanupAfterCompletion(transaction: AnyRef) {
        super.doCleanupAfterCompletion(transaction)
        Session.cleanupResources
    }
}

