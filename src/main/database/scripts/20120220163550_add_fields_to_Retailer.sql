--// add fields to Retailer
-- Migration SQL that makes the change goes here.

alter table Retailer
    add website varchar(255) not null,
    add phone varchar(36) not null,
    add hashTag varchar(36),
    add logo varchar(255),
    add secret varchar(255) not null,
    add deactivateOn timestamp not null default '2038-01-01 00:00:00';

update Retailer set
    website = uuid(),
    phone = uuid(),
    secret = uuid()
    where website = '';

create unique index website on Retailer (website);
create unique index phone on Retailer (phone);
create unique index secret on Retailer (secret);
create unique index hashTag on Retailer (hashTag);
create unique index logo on Retailer (logo);


--//@UNDO
-- SQL to undo the change goes here.

alter table Retailer
    drop website,
    drop phone,
    drop hashTag,
    drop logo,
    drop secret,
    drop deactivateOn;
