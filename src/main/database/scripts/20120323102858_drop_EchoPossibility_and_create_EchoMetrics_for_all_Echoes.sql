--// drop EchoPossibility and create EchoMetrics for all Echoes
-- Migration SQL that makes the change goes here.

alter table Echo
    add step varchar(255) not null,
    modify echoedUserId varchar(36);

alter table EchoMetrics
    modify echoedUserId varchar(36),
    modify creditWindowEndsAt timestamp null default null;

update Echo, EchoPossibility set
    Echo.step = EchoPossibility.step
where Echo.echoPossibilityId = EchoPossibility.id;

insert into Echo (
    id,
    updatedOn,
    createdOn,
    retailerId,
    retailerSettingsId,
    customerId,
    productId,
    boughtOn,
    orderId,
    price,
    imageUrl,
    echoPossibilityId,
    landingPageUrl,
    productName,
    category,
    brand,
    description,
    echoClickId,
    step,
    echoMetricsId)
select
    uuid(),
    EP.updatedOn,
    EP.createdOn,
    EP.retailerId,
    (select id
        from RetailerSettings
        where RetailerSettings.retailerId = EP.retailerId
        and RetailerSettings.activeOn < EP.createdOn
        order by RetailerSettings.activeOn desc
        limit 1) as retailerSettingsId,
    EP.customerId,
    EP.productId,
    EP.boughtOn,
    EP.orderId,
    EP.price,
    EP.imageUrl,
    EP.id,
    EP.landingPageUrl,
    EP.productName,
    EP.category,
    EP.brand,
    EP.description,
    EP.echoClickId,
    EP.step,
    uuid()
from EchoPossibility EP
where EP.echoId is null;

insert into EchoMetrics(
    id,
    clicks,
    createdOn,
    credit,
    echoId,
    fee,
    price,
    residualClicks,
    residualCredit,
    residualFee,
    retailerId,
    retailerSettingsId,
    totalClicks,
    updatedOn)
select
    E.echoMetricsId,
    0,
    E.createdOn,
    0,
    E.id,
    0,
    E.price,
    0,
    0,
    0,
    E.retailerId,
    E.retailerSettingsId,
    0,
    E.updatedOn
from Echo E
where E.echoedUserId is null;


drop table EchoPossibility;


--//@UNDO
-- SQL to undo the change goes here.

create table EchoPossibility (
    id varchar(255) not null,
    updatedOn timestamp not null default CURRENT_TIMESTAMP,
    createdOn timestamp not null default '0000-00-00 00:00:00',
    retailerId varchar(36) not null,
    customerId varchar(255) not null,
    productId varchar(255) not null,
    boughtOn timestamp not null default '0000-00-00 00:00:00',
    step varchar(255) not null,
    orderId varchar(255) not null,
    price float(12, 0) not null,
    imageUrl varchar(1024) not null,
    echoedUserId varchar(36),
    echoId varchar(36),
    landingPageUrl varchar(1024),
    productName varchar(255),
    category varchar(255),
    brand varchar(255),
    description varchar(1024),
    echoClickId varchar(256),
    primary key (id)
);
create unique index echoId on Echoed.EchoPossibility (echoId);
create index retailerId on Echoed.EchoPossibility (retailerId);

insert into EchoPossibility (
    id,
    updatedOn,
    createdOn,
    retailerId,
    customerId,
    productId,
    boughtOn,
    step,
    orderId,
    price,
    imageUrl,
    echoedUserId,
    echoId,
    landingPageUrl,
    productName,
    category,
    brand,
    description,
    echoClickId)
select
    echoPossibilityId,
    updatedOn,
    createdOn,
    retailerId,
    customerId,
    productId,
    boughtOn,
    step,
    orderId,
    price,
    imageUrl,
    echoedUserId,
    id,
    landingPageUrl,
    productName,
    category,
    brand,
    description,
    echoClickId
from Echo;

-- echoedUserId should only be null if the Echo was never echoed...
delete from Echo
where echoedUserId is null;

delete from EchoMetrics
where echoedUserId is null;

alter table Echo
    drop step,
    modify echoedUserId varchar(36) not null;

alter table EchoMetrics
    modify echoedUserId varchar(36) not null,
    modify creditWindowEndsAt timestamp not null default '0000-00-00 00:00:00';

