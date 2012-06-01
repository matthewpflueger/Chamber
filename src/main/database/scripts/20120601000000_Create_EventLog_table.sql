--// Create_EventLog_table
-- Migration SQL that makes the change goes here.

create table EventLog (
    id varchar(36) not null,
    updatedOn timestamp not null default CURRENT_TIMESTAMP,
    createdOn timestamp not null default '0000-00-00 00:00:00',
    name varchar(255) not null,
    ref varchar(255) not null,
    refId varchar(36) not null,
    primary key (id)
);

create index name on EventLog (name);
create index ref on EventLog (ref);
create index refId on EventLog (refId);

--//@UNDO
-- SQL to undo the change goes here.

drop table EventLog;

