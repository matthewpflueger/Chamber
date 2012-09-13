--// Convert_Partner_and_PartnerSettings_to_use_longs
-- Migration SQL that makes the change goes here.

alter table Partner
    add updatedOnLong bigint unsigned not null default 0,
    add createdOnLong bigint unsigned not null default 0,
    add deactivateOnLong bigint unsigned not null default 0;

update Partner as T inner join Partner as T1 on T.id = T1.id set
    T.updatedOnLong = T1.updatedOn + 0,
    T.createdOnLong = T1.createdOn + 0,
    T.deactivateOnLong = T1.deactivateOn + 0;

alter table Partner
    drop updatedOn,
    drop createdOn,
    drop deactivateOn,
    change updatedOnLong updatedOn bigint unsigned not null default 0 after id,
    change createdOnLong createdOn bigint unsigned not null default 0 after updatedOn,
    change deactivateOnLong deactivateOn bigint unsigned not null default 0 after createdOn;


alter table PartnerSettings
    drop index partnerId_2,
    add updatedOnLong bigint unsigned not null default 0,
    add createdOnLong bigint unsigned not null default 0,
    add activeOnLong bigint unsigned not null default 0,
    add couponExpiresOnLong bigint unsigned not null default 0;

update PartnerSettings as T inner join PartnerSettings as T1 on T.id = T1.id set
    T.updatedOnLong = T1.updatedOn + 0,
    T.createdOnLong = T1.createdOn + 0,
    T.activeOnLong = T1.activeOn + 0,
    T.couponExpiresOnLong = T1.couponExpiresOn + 0;

alter table PartnerSettings
    drop updatedOn,
    drop createdOn,
    drop activeOn,
    drop couponExpiresOn,
    change updatedOnLong updatedOn bigint unsigned not null default 0 after id,
    change createdOnLong createdOn bigint unsigned not null default 0 after updatedOn,
    change activeOnLong activeOn bigint unsigned not null default 0 after createdOn,
    change couponExpiresOnLong couponExpiresOn bigint unsigned not null default 0 after activeOn;

create unique index partnerId_2 on PartnerSettings (partnerId, activeOn);
create index activeOn on PartnerSettings (activeOn);


--//@UNDO
-- SQL to undo the change goes here.


alter table Partner
    add updatedOnDate timestamp not null default CURRENT_TIMESTAMP,
    add createdOnDate timestamp not null default '0000-00-00 00:00:00',
    add deactivateOnDate timestamp not null default '0000-00-00 00:00:00';

update Partner as T inner join Partner T1 on T.id = T1.id set
    T.updatedOnDate = T1.updatedOn + '',
    T.createdOnDate = T1.createdOn + '',
    T.deactivateOnDate = T1.deactivateOn + '';

alter table Partner
    drop updatedOn,
    drop createdOn,
    drop deactivateOn,
    change updatedOnDate updatedOn timestamp not null default CURRENT_TIMESTAMP after id,
    change createdOnDate createdOn timestamp not null default '1971-01-01 00:00:00' after updatedOn,
    change deactivateOnDate deactivateOn timestamp not null default '1971-01-01 00:00:00' after createdOn;


alter table PartnerSettings
    drop index partnerId_2,
    add updatedOnDate timestamp not null default CURRENT_TIMESTAMP,
    add createdOnDate timestamp not null default '0000-00-00 00:00:00',
    add activeOnDate timestamp not null default '0000-00-00 00:00:00',
    add couponExpiresOnDate timestamp not null default '0000-00-00 00:00:00';

update PartnerSettings as T inner join PartnerSettings T1 on T.id = T1.id set
    T.updatedOnDate = T1.updatedOn + '',
    T.createdOnDate = T1.createdOn + '',
    T.activeOnDate = T1.activeOn + '',
    T.couponExpiresOnDate = T1.couponExpiresOn + '';

alter table PartnerSettings
    drop updatedOn,
    drop createdOn,
    drop activeOn,
    drop couponExpiresOn,
    change updatedOnDate updatedOn timestamp not null default CURRENT_TIMESTAMP after id,
    change createdOnDate createdOn timestamp not null default '1971-01-01 00:00:00' after updatedOn,
    change activeOnDate activeOn timestamp not null default '1971-01-01 00:00:00' after createdOn,
    change couponExpiresOnDate couponExpiresOn timestamp not null default '1971-01-01 00:00:00' after activeOn;

create unique index partnerId_2 on PartnerSettings (partnerId, activeOn);
create index activeOn on PartnerSettings (activeOn);


