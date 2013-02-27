--// Create_Link_table
-- Migration SQL that makes the change goes here.

create table Link (
    id varchar(36) not null,
    updatedOn bigint unsigned not null default 0,
    createdOn bigint unsigned not null default 0,
    echoedUserId varchar(36) not null,
    partnerId varchar(36) not null,
    partnerHandle varchar(36) not null,
    partnerSettingsId varchar(36) not null,
    storyId varchar(36) not null,
    chapterId varchar(36) not null,
    url varchar(255) not null,
    description varchar(255) null,
    pageTitle varchar(255) null,
    imageId varchar(36) null,
    primary key (id)
);

create index storyId on Link (storyId);
create index chapterId_url on Link (chapterId, url);
create index echoedUserId on Link (echoedUserId);

--//@UNDO
-- SQL to undo the change goes here.

drop table Link;

