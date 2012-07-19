--// Adding Tag Table and Tag to Story
-- Migration SQL that makes the change goes here.

create table Tag (
    id varchar(36) not null,
    primary key(id)
);

alter table Story
    add column tag varchar(36) null;

--//@UNDO
-- SQL to undo the change goes here.

drop table Tag;

alter table Story
    drop column tag;
