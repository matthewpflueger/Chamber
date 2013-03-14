--// Make_Story_title_nullable
-- Migration SQL that makes the change goes here.

alter table Story
  modify title varchar(255) null;


--//@UNDO
-- SQL to undo the change goes here.


