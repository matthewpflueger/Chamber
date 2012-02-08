--// create EchoMetrics table
-- Migration SQL that makes the change goes here.

create table AdminUser (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    name varchar(255) not null,
    email varchar(255) not null unique key,
    salt varchar(255) not null unique key,
    password varchar(255) not null unique key,
    index (email)
);

--//@UNDO
-- SQL to undo the change goes here.

drop table AdminUser;

