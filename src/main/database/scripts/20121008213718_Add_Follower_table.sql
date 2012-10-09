--// Add_Follow_table
-- Migration SQL that makes the change goes here.

create table Follower (
    id varchar(36) not null,
    updatedOn bigint unsigned not null,
    createdOn bigint unsigned not null,
    ref varchar(255) not null,
    refId varchar(36) not null,
    echoedUserId varchar(36) not null,
    primary key (id));

create index ref_refId on Follower (ref, refId);
create index echoedUserId on Follower (echoedUserId);

--//@UNDO
-- SQL to undo the change goes here.

drop table Follower;

