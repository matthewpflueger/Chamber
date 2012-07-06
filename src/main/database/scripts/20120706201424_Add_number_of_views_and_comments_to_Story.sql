--// Add_number_of_views_and_comments_to_Story
-- Migration SQL that makes the change goes here.

alter table Story
    add column views int not null default 0,
    add column comments int not null default 0;

--//@UNDO
-- SQL to undo the change goes here.

alter table Story
    drop column views,
    drop column comments;



