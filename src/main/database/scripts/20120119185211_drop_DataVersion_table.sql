--// drop DataVersion table
-- Migration SQL that makes the change goes here.

drop table if exists DatabaseVersion;

--//@UNDO
-- SQL to undo the change goes here.

create table DatabaseVersion (
    id integer auto_increment not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null
);



