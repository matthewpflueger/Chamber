--// Add filtered to EchoClick
-- Migration SQL that makes the change goes here.

alter table EchoClick
        add column filtered int not null default 0,
        add index filtered(filtered);

--//@UNDO
-- SQL to undo the change goes here.

alter table EchoClick
        drop filtered,
        drop index filtered;


