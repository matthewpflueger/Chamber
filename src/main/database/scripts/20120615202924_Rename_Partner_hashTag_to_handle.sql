--// Rename_Partner_hashTag_to_handle
-- Migration SQL that makes the change goes here.

drop index hashTag on Partner;

alter table Partner
    change hashTag handle varchar(36) null;

create unique index handle on Partner (handle);

--//@UNDO
-- SQL to undo the change goes here.

drop index handle on Partner;

alter table Partner
    change handle hashTag varchar(36) null;

create unique index hashTag on Partner (hashTag);
