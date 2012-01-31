--// Added EchoClick to EchoPossibility and Echo
-- Migration SQL that makes the change goes here.

alter table EchoPossibility
    add echoClickId varchar(256) null;

alter table Echo
    add echoClickId varchar(256) null;

--//@UNDO
-- SQL to undo the change goes here.

alter table Echo
    drop echoClickId;

alter table EchoPossibility
    drop echoClickId;




