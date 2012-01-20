--// create EchoMetrics table
-- Migration SQL that makes the change goes here.

create table EchoMetrics (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    echoId varchar(36) not null unique key,
    echoedUserId varchar(36) not null,
    retailerId varchar(36) not null,
    retailerSettingsId varchar(36) not null,
    price float not null,
    totalClicks integer not null,
    clicks integer not null,
    credit float not null,
    fee float not null,
    residualClicks integer not null,
    residualCredit float not null,
    residualFee float not null,
    index(echoId),
    index(echoedUserId),
    index(retailerId),
    index(retailerSettingsId)
);

--//@UNDO
-- SQL to undo the change goes here.

drop table EchoMetrics;

