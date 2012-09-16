--// Add_Moderation_table
-- Migration SQL that makes the change goes here.

create table Moderation (
    id varchar(36) not null,
    updatedOn bigint unsigned not null,
    createdOn bigint unsigned not null,
    ref varchar(255) not null,
    refId varchar(36) not null,
    moderated boolean default true,
    moderatedBy varchar(255) not null,
    moderatedRef varchar(255) not null,
    moderatedRefId varchar(36) not null,
    primary key (id));

create index ref on Moderation (ref);
create unique index refId on Moderation (refId);
create index moderatedRef on Moderation(moderatedRef);
create index moderatedRefId on Moderation(moderatedRefId);


--//@UNDO
-- SQL to undo the change goes here.

drop table Moderation;
