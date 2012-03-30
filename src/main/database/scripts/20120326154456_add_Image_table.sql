--// add image grab fields
-- Migration SQL that makes the change goes here.


create table Image (
    id varchar(36) not null,
    updatedOn timestamp not null default CURRENT_TIMESTAMP,
    createdOn timestamp not null default '0000-00-00 00:00:00',
    url varchar(255) not null unique key,
    originalUrl varchar(255) null,
    originalWidth integer not null default 0,
    originalHeight integer not null default 0,
    sizedUrl varchar(255) null,
    sizedWidth integer not null default 0,
    sizedHeight integer not null default 0,
    thumbnailUrl varchar(255) null,
    thumbnailWidth integer not null default 0,
    thumbnailHeight integer not null default 0,        
    processedOn timestamp null,
    processedStatus varchar(255) null,
    primary key (id),
    index (processedOn asc),
    index (processedStatus));

insert into Image(
    id,
    updatedOn,
    createdOn,
    url)
select distinct
    uuid(),
    now(),
    now(),
    imageUrl
from Echo;

alter table Echo
    add imageId varchar(36) not null;

update Echo set imageId = (select id from Image where Echo.imageUrl = Image.url);


--//@UNDO
-- SQL to undo the change goes here.

alter table Echo drop imageId;

drop table Image;

