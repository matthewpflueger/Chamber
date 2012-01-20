--// add creditWindow
-- Migration SQL that makes the change goes here.

alter table RetailerSettings
    add creditWindow integer not null;

update RetailerSettings set creditWindow = 168;

alter table EchoMetrics
    add creditWindowEndsAt timestamp not null;

update EchoMetrics, Echo set
    EchoMetrics.creditWindowEndsAt = date_add(Echo.createdOn, interval 168 hour)
    where EchoMetrics.echoId = Echo.id;

--//@UNDO
-- SQL to undo the change goes here.

alter table RetailerSettings
    drop creditWindow;

alter table EchoMetrics
    drop creditWindowEndsAt;

