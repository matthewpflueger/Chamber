--// Add IP address info table
-- Migration SQL that makes the change goes here.
-- "66.202.133.170","66.202.133.170","US","United States","NY","New York","New York","","40.761900","-73.976300","Regus Business Center","Regus Business Center"

create table GeoLocation (
    id varchar(36) not null,
    updatedOn timestamp not null default CURRENT_TIMESTAMP,
    createdOn timestamp not null default '0000-00-00 00:00:00',
    ipAddress varchar(36) not null unique key,
    countryCode varchar(36) null,
    countryName varchar(255) null,
    regionCode varchar(36) null,
    regionName varchar(255) null,
    city varchar(255) null,
    postcode varchar(36) null,
    latitude varchar(36) null,
    longitude varchar(36) null,
    isp varchar(255) null,
    organization varchar(255) null,
    updateStatus varchar(255) null,
    primary key(id),
    index(countryCode),
    index(regionCode),
    index(city),
    index(postcode));

--//@UNDO
-- SQL to undo the change goes here.

drop table GeoLocation;
