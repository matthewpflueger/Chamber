--// Create_tables_for_Story_feature
-- Migration SQL that makes the change goes here.

create table Story (
    id varchar(36) not null,
    updatedOn bigint unsigned not null default 0,
    createdOn bigint unsigned not null default 0,
    echoedUserId varchar(36) not null,
    partnerId varchar(36) not null,
    partnerSettingsId varchar(36) not null,
    imageId varchar(36) not null,
    title varchar(255) not null,
    echoId varchar(36) null,
    productId varchar(255) null,
    primary key (id)
);

create index echoedUserId on Story (echoedUserId);
create index partnerId on Story (partnerId);
create index partnerSettingsId on Story (partnerSettingsId);
create index echoId on Story (echoId);


create table Chapter (
    id varchar(36) not null,
    updatedOn bigint unsigned not null default 0,
    createdOn bigint unsigned not null default 0,
    storyId varchar(36) not null,
    echoedUserId varchar(36) not null,
    partnerId varchar(36) not null,
    partnerSettingsId varchar(36) not null,
    echoId varchar(36) null,
    title varchar(255) not null,
    text varchar(2048) not null,
    publishedOn bigint unsigned not null default 0,
    primary key (id)
);

create index storyId on Chapter (storyId);
create index echoedUserId on Chapter (echoedUserId);
create index partnerId on Chapter (partnerId);
create index partnerSettingsId on Chapter (partnerSettingsId);
create index echoId on Chapter (echoId);


create table ChapterImage (
    id varchar(36) not null,
    updatedOn bigint unsigned not null default 0,
    createdOn bigint unsigned not null default 0,
    storyId varchar(36) not null,
    echoedUserId varchar(36) not null,
    partnerId varchar(36) not null,
    partnerSettingsId varchar(36) not null,
    echoId varchar(36) null,
    chapterId varchar(36) not null,
    imageId varchar(36) not null
);

create index storyId on ChapterImage (storyId);
create index echoedUserId on ChapterImage (echoedUserId);
create index partnerId on ChapterImage (partnerId);
create index partnerSettingsId on ChapterImage (partnerSettingsId);
create index echoId on ChapterImage (echoId);
create index chapterId on ChapterImage (chapterId);
create index imageId on ChapterImage (imageId);


create table Comment (
    id varchar(36) not null,
    updatedOn bigint unsigned not null default 0,
    createdOn bigint unsigned not null default 0,
    storyId varchar(36) not null,
    echoedUserId varchar(36) not null,
    partnerId varchar(36) not null,
    partnerSettingsId varchar(36) not null,
    echoId varchar(36) null,
    chapterId varchar(36) not null,
    byEchoedUserId varchar(36) not null,
    parentCommentId varchar(36) null,
    text varchar(1024) not null
);

create index storyId on Comment (storyId);
create index echoedUserId on Comment (echoedUserId);
create index partnerId on Comment (partnerId);
create index partnerSettingsId on Comment (partnerSettingsId);
create index echoId on Comment (echoId);
create index chapterId on Comment (chapterId);
create index byEchoedUserId on Comment (byEchoedUserId);
create index parentCommentId on Comment (parentCommentId);


--//@UNDO
-- SQL to undo the change goes here.

drop table Comment;
drop table ChapterImage;
drop table Chapter;
drop table Story;

