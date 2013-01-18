--// Add_contentType_and_contentPath_to_Story
-- Migration SQL that makes the change goes here.

alter table Story
    add column contentType varchar(36) not null default 'Story',
    add column contentPath varchar(255) null;

update Story set contentType = 'Story';

--//@UNDO
-- SQL to undo the change goes here.

alter table Story
    drop column contentType,
    drop column contentPath;


