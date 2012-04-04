--// Increase processedStatus in Image
-- Migration SQL that makes the change goes here.

alter table Image
    modify processedStatus varchar(512) not null;


--//@UNDO
-- SQL to undo the change goes here.

update Image set
    processedStatus = left(rtrim(processedStatus), 254);

alter table Image
    modify processedStatus varchar(255) not null;

