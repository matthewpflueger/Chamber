--// Add TopicId To Story
-- Migration SQL that makes the change goes here.

alter table Story
    add column topicId varchar(36) null;


--//@UNDO
-- SQL to undo the change goes here.

alter table Story
    drop topicId;

