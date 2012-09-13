--// Convert_Image_processedOn_to_be_long
-- Migration SQL that makes the change goes here.

alter table Image
    add processedOnLong bigint unsigned not null default 0;

update Image as T inner join Image as T1 on T.id = T1.id set
    T.processedOnLong = T1.updatedOn + 0;

alter table Image
    drop processedOn,
    change processedOnLong processedOn bigint unsigned not null default 0 after createdOn;

create index processedOn on Image (processedOn);


--//@UNDO
-- SQL to undo the change goes here.

alter table Image
    add processedOnDate timestamp not null default '0000-00-00 00:00:00';

update Image as T inner join Image T1 on T.id = T1.id set
    T.processedOnDate = T1.processedOn + '';

alter table Image
    drop processedOn,
    change processedOnDate processedOn timestamp not null default '1971-01-01 00:00:00' after createdOn;

create index processedOn on Image (processedOn);
