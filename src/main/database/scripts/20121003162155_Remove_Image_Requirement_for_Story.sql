--// Remove Image Requirement for Story
-- Migration SQL that makes the change goes here.

alter table Story
    modify imageId varchar(36) null;

--//@UNDO
-- SQL to undo the change goes here.

alter table Story
    modify imageId varchar(36) not null;


