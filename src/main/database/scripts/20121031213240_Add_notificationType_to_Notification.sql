--// Add_notificationType_to_Notification
-- Migration SQL that makes the change goes here.

alter table Notification
    add notificationType varchar(255) null;


--//@UNDO
-- SQL to undo the change goes here.

alter table Notification
    drop notificationType;


