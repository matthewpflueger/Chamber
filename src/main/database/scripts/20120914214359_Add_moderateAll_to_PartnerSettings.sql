--// Add_moderateAll_to_PartnerSettings
-- Migration SQL that makes the change goes here.

alter table PartnerSettings
    add moderateAll boolean not null default false;

--//@UNDO
-- SQL to undo the change goes here.

alter table PartnerSettings
    drop moderateAll;


