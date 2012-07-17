--// Add Admin Filter Column to Story
-- Migration SQL that makes the change goes here.

alter table Story
    add column adminFilter boolean default 0;

--//@UNDO
-- SQL to undo the change goes here.

alter table Story
    drop column adminFilter;

