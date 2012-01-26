--// added forwarded for EchoClick
-- Migration SQL that makes the change goes here.

alter table EchoClick add forwardedFor varchar(256);

--//@UNDO
-- SQL to undo the change goes here.

alter table EchoClick drop forwardedFor;


