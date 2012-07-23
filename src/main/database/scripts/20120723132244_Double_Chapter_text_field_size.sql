--// Double_Chapter_text_field_size
-- Migration SQL that makes the change goes here.

alter table Chapter modify text varchar(4096) not null;

--//@UNDO
-- SQL to undo the change goes here.


