--// Added content Page Title
-- Migration SQL that makes the change goes here.

alter table Story
    add column contentPageTitle varchar(255) null;

update Story set contentType = 'Story';

--//@UNDO
-- SQL to undo the change goes here.

alter table Story
    drop column contentPageTitle;

