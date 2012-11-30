--// Add Topics Table
-- Migration SQL that makes the change goes here.

create table Topic(
    id varchar(36) primary key not null,
    updatedOn bigint unsigned not null,
    createdOn bigint unsigned not null,
    partnerId varchar(36) not null,
    community varchar(36) not null,
    title varchar(255) not null,
    description varchar(2048),
    beginOn bigint unsigned not null,
    endOn bigint unsigned not null);


--//@UNDO
-- SQL to undo the change goes here.


drop table Topic;