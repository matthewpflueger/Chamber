--// Adding Hashtag to PartnerSettings
-- Migration SQL that makes the change goes here.
alter table PartnerSettings
    add column hashtag varchar(32) not null;

--//@UNDO
-- SQL to undo the change goes here.

alter table PartnerSettings
    drop hashtag;


