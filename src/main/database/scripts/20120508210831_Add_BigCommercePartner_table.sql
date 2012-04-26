--// Add_BigCommercePartner_table
-- Migration SQL that makes the change goes here.

create table BigCommercePartner (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    name varchar(255) not null,
    email varchar(255) not null unique key,
    phone varchar(36) not null unique key,
    storeUrl varchar(255) not null unique key,
    businessName varchar(255) not null unique key,
    apiUser varchar(36) not null unique key,
    apiPath varchar(255) not null unique key,
    apiToken varchar(255) not null unique key,
    partnerId varchar(36) not null unique key
);


--//@UNDO
-- SQL to undo the change goes here.

drop table BigCommercePartner;
