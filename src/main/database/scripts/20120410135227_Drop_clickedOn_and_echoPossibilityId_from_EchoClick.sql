--// Drop clickedOn and echoPossibilityId from EchoClick
-- Migration SQL that makes the change goes here.

alter table EchoClick
        drop clickedOn,
        drop echoPossibilityId;

--//@UNDO
-- SQL to undo the change goes here.

alter table EchoClick
        add clickedOn timestamp not null default '0000-00-00 00:00:00',
        add echoPossibilityId varchar(255) not null;

update EchoClick, Echo set
        EchoClick.clickedOn = EchoClick.createdOn,
        EchoClick.echoPossibilityId = Echo.echoPossibilityId
    where EchoClick.echoId = Echo.id;
