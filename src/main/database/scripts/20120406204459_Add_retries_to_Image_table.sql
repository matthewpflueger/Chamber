--// Add retries to Image table
-- Migration SQL that makes the change goes here.

alter table Image
    add retries integer not null default 0;


--//@UNDO
-- SQL to undo the change goes here.

alter table Image
    drop retries;


