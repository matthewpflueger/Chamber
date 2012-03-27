--// Add ShopifyUser Table
-- Migration SQL that makes the change goes here.

create table ShopifyPartner (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    shopifyId varchar(36) not null unique key,
    domain varchar(255) not null unique key,
    name varchar(255) not null unique key,
    zip varchar(5),
    shopOwner varchar(255) not null,
    email varchar(255) not null unique key,
    phone varchar(32)  not null,
    country varchar(255),
    city varchar(255),
    shopifyDomain varchar(255) not null unique key,
    password varchar(255) not null,
    partnerId varchar(255),
    index (email)
);

--//@UNDO
-- SQL to undo the change goes here.

drop table ShopifyPartner;