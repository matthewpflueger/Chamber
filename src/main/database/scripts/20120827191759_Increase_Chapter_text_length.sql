--// Increase_Chapter_text_length
-- Migration SQL that makes the change goes here.

alter table Chapter modify text mediumtext not null;


--//@UNDO
-- SQL to undo the change goes here.


