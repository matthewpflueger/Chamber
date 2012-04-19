--// Add cloudPartnerId to Partner
-- Migration SQL that makes the change goes here.

alter table Partner
        add cloudPartnerId varchar(36) null;

--//@UNDO
-- SQL to undo the change goes here.

alter table Partner
        drop cloudPartnerId;


