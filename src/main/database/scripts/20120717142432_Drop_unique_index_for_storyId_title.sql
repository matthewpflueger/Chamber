--// Drop_unique_index_for_storyId_title
-- Migration SQL that makes the change goes here.

alter table Chapter drop index storyId_title;

--//@UNDO
-- SQL to undo the change goes here.


