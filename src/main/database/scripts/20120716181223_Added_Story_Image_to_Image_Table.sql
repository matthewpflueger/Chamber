--// Added Story Image to Image Table
-- Migration SQL that makes the change goes here.

alter table Image
    add column storyUrl varchar(255) null,
    add column storyWidth int not null default 0,
    add column storyHeight int not null default 0;

--//@UNDO
-- SQL to undo the change goes here.


alter table Image
    drop column storyUrl,
    drop column storyWidth,
    drop column storyHeight;




