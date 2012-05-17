--// Drop_unique_index_phone_on_Partner
-- Migration SQL that makes the change goes here.

alter table Partner
    modify phone varchar(36) null,
    drop index phone;


--//@UNDO
-- SQL to undo the change goes here.

update Partner set phone = uuid() where phone is null;
create unique index phone on Partner (phone);


