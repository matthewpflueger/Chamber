--// Rename partner website to partner domain
-- Migration SQL that makes the change goes here.

alter table Partner
        change website domain varchar(255) not null;

--//@UNDO
-- SQL to undo the change goes here.

alter table Partner
        change domain website varchar(255) not null;

