--// Facebook email can be null now
-- Migration SQL that makes the change goes here.

alter table FacebookUser
    modify email varchar(255) not null default "unknown@echoed.com";

--//@UNDO
-- SQL to undo the change goes here.

update FacebookUser set email = "unknown@echoed.com"
where email is null;

alter table FacebookUser
    modify email varchar(255) not null;
