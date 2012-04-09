--// Add retries to FacebookPost table
-- Migration SQL that makes the change goes here.

alter table FacebookPost
    add retries integer not null default 0;


--//@UNDO
-- SQL to undo the change goes here.

alter table FacebookPost
    drop retries;
