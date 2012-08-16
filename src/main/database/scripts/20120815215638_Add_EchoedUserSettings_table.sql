--// Add_EchoedUserSettings_table
-- Migration SQL that makes the change goes here.

create table EchoedUserSettings(
    id varchar(36) primary key not null,
    updatedOn bigint unsigned not null,
    createdOn bigint unsigned not null,
    echoedUserId varchar(36) not null,
    receiveNotificationEmail boolean default true not null);

insert into EchoedUserSettings(id, updatedOn, createdOn, echoedUserId)
select uuid(), utc_timestamp() + 0, utc_timestamp() + 0, id from EchoedUser;

--//@UNDO
-- SQL to undo the change goes here.

drop table EchoedUserSettings;


