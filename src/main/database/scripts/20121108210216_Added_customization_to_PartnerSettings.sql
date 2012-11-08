--// Added customization to PartnerSettings
-- Migration SQL that makes the change goes here.

alter table PartnerSettings
    add customization varchar(4096) null;


--//@UNDO
-- SQL to undo the change goes here.

alter table PartnerSettings
    drop customization;

