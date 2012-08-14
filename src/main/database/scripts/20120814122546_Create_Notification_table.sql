--// Create_Notification_table
-- Migration SQL that makes the change goes here.

create table Notification(
    id varchar(36) primary key not null,
    updatedOn bigint unsigned not null,
    createdOn bigint unsigned not null,
    echoedUserId varchar(36) not null,
    originId varchar(36) not null,
    category varchar(255) not null,
    emailedOn bigint unsigned null,
    readOn bigint unsigned null,
    value text not null);

--//@UNDO
-- SQL to undo the change goes here.

drop table Notification;

