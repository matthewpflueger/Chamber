--// Set_default_value_for_published
-- Migration SQL that makes the change goes here.

update Chapter set publishedOn = utc_timestamp() + 0;

--//@UNDO
-- SQL to undo the change goes here.


