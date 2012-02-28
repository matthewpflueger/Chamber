--// Add UserAgent to EchoClick
-- Migration SQL that makes the change goes here.

alter table EchoClick
    add userAgent varchar(255);

--//@UNDO
-- SQL to undo the change goes here.

alter table EchoClick
    drop userAgent;


