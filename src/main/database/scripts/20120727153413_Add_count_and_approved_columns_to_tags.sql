--// Add count and approved columns to tags
-- Migration SQL that makes the change goes here.

alter table Tag
    add column counter int not null default 0,
    add column approved int not null default 0;


--//@UNDO
-- SQL to undo the change goes here.

alter table Tag
    drop column counter,
    drop column approved;


