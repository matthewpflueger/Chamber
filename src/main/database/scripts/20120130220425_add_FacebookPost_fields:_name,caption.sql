--// add FacebookPost fields: name,caption
-- Migration SQL that makes the change goes here.

alter table FacebookPost
    add name varchar(255) not null,
    add caption varchar(1024) not null;

--//@UNDO
-- SQL to undo the change goes here.

alter table FacebookPost
    drop name,
    drop caption;
