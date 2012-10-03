--// Add Value Column to Vote Table
-- Migration SQL that makes the change goes here.

alter table Vote
    add column value tinyint default 1;

--//@UNDO
-- SQL to undo the change goes here.

alter table Vote
    drop column value;
