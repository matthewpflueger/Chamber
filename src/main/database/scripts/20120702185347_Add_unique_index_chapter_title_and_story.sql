--// Add_unique_index_chapter_title_and_story
-- Migration SQL that makes the change goes here.

delete S, C, CI, CMT from Story S
left join Chapter C on C.storyId = S.id
left join ChapterImage CI on CI.storyId = S.id
left join Comment CMT on CMT.storyId = S.id;

create unique index storyId_title on Chapter(storyId, title);

--//@UNDO
-- SQL to undo the change goes here.

alter table Chapter drop index storyId_title;
