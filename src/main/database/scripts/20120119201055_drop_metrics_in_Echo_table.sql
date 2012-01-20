--// drop metrics in Echo table
-- Migration SQL that makes the change goes here.

alter table Echo
    add echoMetricsId varchar(36) not null;

update Echo, EchoMetrics set
    Echo.echoMetricsId = EchoMetrics.id
    where Echo.id = EchoMetrics.echoId;

create unique index echoMetricsId on Echo (echoMetricsId);

alter table Echo
    drop totalClicks,
    drop credit,
    drop fee;


--//@UNDO
-- SQL to undo the change goes here.

alter table Echo
    add totalClicks integer not null,
    add credit float not null,
    add fee float not null;

update Echo, EchoMetrics set
    Echo.totalClicks = EchoMetrics.totalClicks,
    Echo.credit = EchoMetrics.credit,
    Echo.fee = EchoMetrics.fee
    where Echo.id = EchoMetrics.echoId;

alter table Echo
    drop echoMetricsId;

