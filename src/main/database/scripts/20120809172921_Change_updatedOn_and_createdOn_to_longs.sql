--// Change_updatedOn_and_createdOn_to_longs
-- Migration SQL that makes the change goes here.

alter table EchoedUser
    add updatedOnLong bigint unsigned not null default 0,
    add createdOnLong bigint unsigned not null default 0;

update EchoedUser as T inner join EchoedUser as T1 on T.id = T1.id set
    T.updatedOnLong = T1.updatedOn + 0,
    T.createdOnLong = T1.createdOn + 0;

alter table EchoedUser
    drop updatedOn,
    drop createdOn,
    change updatedOnLong updatedOn bigint unsigned not null default 0 after id,
    change createdOnLong createdOn bigint unsigned not null default 0 after updatedOn;


alter table FacebookUser
    add updatedOnLong bigint unsigned not null default 0,
    add createdOnLong bigint unsigned not null default 0;

update FacebookUser as T inner join FacebookUser as T1 on T.id = T1.id set
    T.updatedOnLong = T1.updatedOn + 0,
    T.createdOnLong = T1.createdOn + 0;

alter table FacebookUser
    drop updatedOn,
    drop createdOn,
    change updatedOnLong updatedOn bigint unsigned not null default 0 after id,
    change createdOnLong createdOn bigint unsigned not null default 0 after updatedOn;


alter table TwitterUser
    add updatedOnLong bigint unsigned not null default 0,
    add createdOnLong bigint unsigned not null default 0;

update TwitterUser as T inner join TwitterUser as T1 on T.id = T1.id set
    T.updatedOnLong = T1.updatedOn + 0,
    T.createdOnLong = T1.createdOn + 0;

alter table TwitterUser
    drop updatedOn,
    drop createdOn,
    change updatedOnLong updatedOn bigint unsigned not null default 0 after id,
    change createdOnLong createdOn bigint unsigned not null default 0 after updatedOn;


alter table AdminUser
    add updatedOnLong bigint unsigned not null default 0,
    add createdOnLong bigint unsigned not null default 0;

update AdminUser as T inner join AdminUser as T1 on T.id = T1.id set
    T.updatedOnLong = T1.updatedOn + 0,
    T.createdOnLong = T1.createdOn + 0;

alter table AdminUser
    drop updatedOn,
    drop createdOn,
    change updatedOnLong updatedOn bigint unsigned not null default 0 after id,
    change createdOnLong createdOn bigint unsigned not null default 0 after updatedOn;


--//@UNDO
-- SQL to undo the change goes here.


alter table EchoedUser
    add updatedOnDate timestamp not null default CURRENT_TIMESTAMP,
    add createdOnDate timestamp not null default '0000-00-00 00:00:00';

update EchoedUser as T inner join EchoedUser T1 on T.id = T1.id set
    T.updatedOnDate = T1.updatedOn + '',
    T.createdOnDate = T1.createdOn + '';

alter table EchoedUser
    drop updatedOn,
    drop createdOn,
    change updatedOnDate updatedOn timestamp not null default CURRENT_TIMESTAMP after id,
    change createdOnDate createdOn timestamp not null default '1971-01-01 00:00:00' after updatedOn;


alter table FacebookUser
    add updatedOnDate timestamp not null default CURRENT_TIMESTAMP,
    add createdOnDate timestamp not null default '0000-00-00 00:00:00';

update FacebookUser as T inner join FacebookUser T1 on T.id = T1.id set
    T.updatedOnDate = T1.updatedOn + '',
    T.createdOnDate = T1.createdOn + '';

alter table FacebookUser
    drop updatedOn,
    drop createdOn,
    change updatedOnDate updatedOn timestamp not null default CURRENT_TIMESTAMP after id,
    change createdOnDate createdOn timestamp not null default '1971-01-01 00:00:00' after updatedOn;


alter table TwitterUser
    add updatedOnDate timestamp not null default CURRENT_TIMESTAMP,
    add createdOnDate timestamp not null default '0000-00-00 00:00:00';

update TwitterUser as T inner join TwitterUser T1 on T.id = T1.id set
    T.updatedOnDate = T1.updatedOn + '',
    T.createdOnDate = T1.createdOn + '';

alter table TwitterUser
    drop updatedOn,
    drop createdOn,
    change updatedOnDate updatedOn timestamp not null default CURRENT_TIMESTAMP after id,
    change createdOnDate createdOn timestamp not null default '1971-01-01 00:00:00' after updatedOn;


alter table AdminUser
    add updatedOnDate timestamp not null default CURRENT_TIMESTAMP,
    add createdOnDate timestamp not null default '0000-00-00 00:00:00';

update AdminUser as T inner join AdminUser T1 on T.id = T1.id set
    T.updatedOnDate = T1.updatedOn + '',
    T.createdOnDate = T1.createdOn + '';

alter table AdminUser
    drop updatedOn,
    drop createdOn,
    change updatedOnDate updatedOn timestamp not null default CURRENT_TIMESTAMP after id,
    change createdOnDate createdOn timestamp not null default '1971-01-01 00:00:00' after updatedOn;
