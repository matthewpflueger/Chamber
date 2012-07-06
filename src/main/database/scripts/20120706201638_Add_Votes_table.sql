--// Add_Votes_table
-- Migration SQL that makes the change goes here.

create table Votes (
    id varchar(36) not null primary key,
    updatedOn bigint unsigned not null default 0,
    createdOn bigint unsigned not null default 0,
    object varchar(255) not null,
    objectId varchar(36) not null,
    echoedUserId varchar(36) not null,
    unique index(echoedUserId, objectId)
);

--//@UNDO
-- SQL to undo the change goes here.

drop table Votes;
