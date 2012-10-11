--// Changed Story Tag To Community
-- Migration SQL that makes the change goes here.

alter table Story
    change tag community varchar(36) null;

--//@UNDO
-- SQL to undo the change goes here.

alter table Story
    change community tag varchar(36) null;
