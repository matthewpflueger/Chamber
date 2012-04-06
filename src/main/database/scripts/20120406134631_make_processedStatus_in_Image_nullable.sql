--// make processedStatus in Image nullable
-- Migration SQL that makes the change goes here.

alter table Image
    modify processedStatus varchar(512) null;


--//@UNDO
-- SQL to undo the change goes here.

update Image set processedStatus = "unknown" where processedStatus is null;

alter table Image
    modify processedStatus varchar(512) not null;

