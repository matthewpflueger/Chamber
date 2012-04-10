--// Add view to Echo for A-B testing
-- Migration SQL that makes the change goes here.

alter table Echo
        add view varchar(36) null;

--//@UNDO
-- SQL to undo the change goes here.

alter table Echo
        drop view;


