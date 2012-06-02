--// Facebook_email_can_be_null_again
-- Migration SQL that makes the change goes here.

alter table FacebookUser
    modify email varchar(255) null default "unknown@echoed.com";

--//@UNDO
-- SQL to undo the change goes here.

update FacebookUser set email = "unknown@echoed.com"
where email is null;

alter table FacebookUser
    modify email varchar(255) not null;


