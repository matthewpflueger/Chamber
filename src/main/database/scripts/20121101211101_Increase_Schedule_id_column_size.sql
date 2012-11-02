--// Increase_Schedule_id_column_size
-- Migration SQL that makes the change goes here.

alter table Schedule modify id varchar(255) not null;

--//@UNDO
-- SQL to undo the change goes here.


