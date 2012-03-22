--// Facebook gender can be null now
-- Migration SQL that makes the change goes here.

alter table FacebookUser
    modify gender varchar(36) not null default "unknown";

--//@UNDO
-- SQL to undo the change goes here.

update FacebookUser set gender = "unknown"
where gender is null;

alter table FacebookUser
    modify gender varchar(36) not null;
