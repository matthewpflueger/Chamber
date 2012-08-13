package com.echoed.chamber.services.adminuser

import com.echoed.chamber.services.{CreatedEvent, UpdatedEvent, Event}
import com.echoed.chamber.domain.AdminUser


trait AdminUserEvent extends Event

import com.echoed.chamber.services.adminuser.{AdminUserEvent => AUE}


private[services] case class AdminUserCreated(adminUser: AdminUser) extends AUE with CreatedEvent
private[services] case class AdminUserUpdated(adminUser: AdminUser) extends AUE with UpdatedEvent
