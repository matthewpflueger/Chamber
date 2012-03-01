--// increase step field size
-- Migration SQL that makes the change goes here.

alter table EchoPossibility
    modify step varchar(255) not null;

--//@UNDO
-- SQL to undo the change goes here.

update EchoPossibility set
    step = left(rtrim(step), 36);

alter table EchoPossibility
    modify step varchar(36) not null;


