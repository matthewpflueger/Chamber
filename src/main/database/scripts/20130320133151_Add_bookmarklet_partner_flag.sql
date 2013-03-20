--// Add_bookmarklet_partner_flag
-- Migration SQL that makes the change goes here.

alter table Partner add column bookmarklet boolean not null default true;

update Partner P
join Partner P1 on P1.id = P.id
set P.bookmarklet = false
where binary P1.name <> P.domain and P1.createdOn < 20130201000000;

--//@UNDO
-- SQL to undo the change goes here.

alter table Partner drop column bookmarklet;

