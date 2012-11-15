--// Increase_comment_text_size
-- Migration SQL that makes the change goes here.

alter table Comment modify text mediumtext not null;

--//@UNDO
-- SQL to undo the change goes here.
