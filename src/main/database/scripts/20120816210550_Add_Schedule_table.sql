--// Add_Schedule_table
-- Migration SQL that makes the change goes here.

create table Schedule(
    id varchar(36) primary key not null,
    updatedOn bigint unsigned not null,
    createdOn bigint unsigned not null,
    schedulePattern varchar(255) not null,
    messageClass varchar(255) not null,
    message text);

--//@UNDO
-- SQL to undo the change goes here.

drop table Schedule;


