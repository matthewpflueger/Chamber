--// add description field to Echo and Echo Possibility
-- Migration SQL that makes the change goes here.

alter table EchoPossibility
    add description varchar(1024) null;

alter table Echo
    add description varchar(1024) null;


--//@UNDO
-- SQL to undo the change goes here.

alter table EchoPossibility
    drop description;

alter table Echo
    drop description;

