--// Added new column EchoPossibility to echoClick
-- Migration SQL that makes the change goes here.

alter table EchoClick
    add echoPossibilityId varchar(255) not null,
    modify echoId varchar(36);

--//@UNDO
-- SQL to undo the change goes here.

alter table EchoClick
    drop echoPossibilityId,
    modify echoId varchar(36) not null;

