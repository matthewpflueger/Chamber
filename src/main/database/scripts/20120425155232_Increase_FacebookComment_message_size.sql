--// Increase FacebookComment message size
-- Migration SQL that makes the change goes here.

alter table FacebookComment
        modify message varchar(1024) not null;


--//@UNDO
-- SQL to undo the change goes here.

update FacebookComment set
        message = left(rtrim(message), 254);

alter table FacebookComment
        modify message varchar(255) not null;



