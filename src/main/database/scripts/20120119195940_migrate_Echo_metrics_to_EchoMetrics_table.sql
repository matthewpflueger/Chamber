--// migrate Echo metrics to EchoMetrics table
-- Migration SQL that makes the change goes here.

insert into EchoMetrics (
    id,
    createdOn,
    echoId,
    echoedUserId,
    retailerId,
    retailerSettingsId,
    price,
    totalClicks,
    clicks,
    credit,
    fee,
    residualClicks,
    residualCredit,
    residualFee)
select
    uuid(),
    createdOn,
    id,
    echoedUserId,
    retailerId,
    retailerSettingsId,
    price,
    totalClicks,
    totalClicks,
    credit,
    fee,
    0,
    0,
    0
from Echo;


--//@UNDO
-- SQL to undo the change goes here.

truncate EchoMetrics;

