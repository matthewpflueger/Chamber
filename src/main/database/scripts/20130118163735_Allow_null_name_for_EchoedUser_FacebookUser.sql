--// Allow_null_name_for_EchoedUser_FacebookUser
-- Migration SQL that makes the change goes here.

alter table EchoedUser
    modify name varchar(255) null default 'unknown';

alter table FacebookUser
    modify name varchar(255) null default 'unknown';


--//@UNDO
-- SQL to undo the change goes here.

alter table EchoedUser
    modify name varchar(255) not null;

alter table FacebookUser
    modify name varchar(255) not null;

