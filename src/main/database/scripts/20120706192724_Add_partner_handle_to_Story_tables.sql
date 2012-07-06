--// Add_partner_handle_to_Story_tables
-- Migration SQL that makes the change goes here.

alter table Story add column partnerHandle varchar(36) null;
alter table Chapter add column partnerHandle varchar(36) null;
alter table ChapterImage add column partnerHandle varchar(36) null;
alter table Comment add column partnerHandle varchar(36) null;

--//@UNDO
-- SQL to undo the change goes here.

alter table Story drop column partnerHandle;
alter table Chapter drop column partnerHandle;
alter table ChapterImage drop column partnerHandle;
alter table Comment drop column partnerHandle;

