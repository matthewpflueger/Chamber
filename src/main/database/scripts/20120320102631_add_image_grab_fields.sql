--// add image grab fields
-- Migration SQL that makes the change goes here.

alter table EchoPossibility
    add imageOriginalUrl varchar(1024) not null,
    add imageWidth integer not null default 0,
    add imageHeight integer not null default 0,
    add imageGrabbedOn timestamp null,
    add imageValidatedOn timestamp null;

alter table Echo
    add imageOriginalUrl varchar(1024) not null,
    add imageWidth integer not null default 0,
    add imageHeight integer not null default 0,
    add imageGrabbedOn timestamp null,
    add imageValidatedOn timestamp null;

update EchoPossibility set
    imageOriginalUrl = imageUrl;

update Echo set
    imageOriginalUrl = imageUrl;


--//@UNDO
-- SQL to undo the change goes here.

alter table Echo
    drop imageOriginalUrl,
    drop imageWidth,
    drop imageHeight,
    drop imageGrabbedOn,
    drop imageValidatedOn;

alter table EchoPossibility
    drop imageOriginalUrl,
    drop imageWidth,
    drop imageHeight,
    drop imageGrabbedOn,
    drop imageValidatedOn;

