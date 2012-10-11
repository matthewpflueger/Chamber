--// Restructured Votes Table
-- Migration SQL that makes the change goes here.
alter table Votes
    change object ref varchar(255) not null;

alter table Votes
    change objectId refId varchar(36) not null;

alter table Votes
    rename to Vote;

--//@UNDO
-- SQL to undo the change goes here.
alter table Vote
    change ref object varchar(255) not null;

alter table Vote
    change refId objectId varchar(36) not null;


alter table Vote
    rename to Votes;

