--// remove not null on EchoClick referrerUrl
-- Migration SQL that makes the change goes here.

alter table EchoClick
change referrerUrl referrerUrl varchar(1024);

update EchoClick set referrerUrl = null where referrerUrl = "null";

--//@UNDO
-- SQL to undo the change goes here.

update EchoClick set referrerUrl = "null" where referrerUrl is null;

alter table EchoClick
change referrerUrl referrerUrl varchar(1024) not null;

