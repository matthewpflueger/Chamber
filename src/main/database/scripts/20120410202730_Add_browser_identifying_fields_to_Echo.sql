--// Add browser identifying fields to Echo
-- Migration SQL that makes the change goes here.

alter table Echo
        add browserId varchar(36) null,
        add ipAddress varchar(36) null,
        add userAgent varchar(255) null,
        add referrerUrl varchar(1024) null;

create index browserId on Echo (browserId);
create index ipAddress on Echo (ipAddress);
create index userAgent on Echo (userAgent);
create index referrerUrl on Echo (referrerUrl);

-- forwardedFor is always the ipAddress
alter table EchoClick
        add browserId varchar(36) null,
        drop forwardedFor;

create index browserId on EchoClick (browserId);
create index ipAddress on EchoClick (ipAddress);
create index userAgent on EchoClick (userAgent);
create index referrerUrl on EchoClick (referrerUrl);



--//@UNDO
-- SQL to undo the change goes here.

alter table EchoClick
    add forwardedFor varchar(256) not null,
    drop browserId;

drop index ipAddress on EchoClick;
drop index userAgent on EchoClick;
drop index referrerUrl on EchoClick;

update EchoClick set forwardedFor = EchoClick.ipAddress;

alter table Echo
    drop browserId,
    drop ipAddress,
    drop userAgent,
    drop referrerUrl;
