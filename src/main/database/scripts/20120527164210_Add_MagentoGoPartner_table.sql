--// Add_MagentoGo_table
-- Migration SQL that makes the change goes here.

create table MagentoGoPartner (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    name varchar(255) not null,
    email varchar(255) not null unique key,
    phone varchar(36) not null,
    storeUrl varchar(255) not null unique key,
    businessName varchar(255) not null unique key,
    apiUser varchar(36) not null,
    apiPath varchar(255) not null unique key,
    apiKey varchar(255) not null,
    partnerId varchar(36) not null unique key
);


--//@UNDO
-- SQL to undo the change goes here.

drop table MagentoGoPartner;
