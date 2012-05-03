--// Add_NetworkSolutionsPartner_table
-- Migration SQL that makes the change goes here.

create table NetworkSolutionsPartner (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    name varchar(255) not null,
    email varchar(255) not null unique key,
    phone varchar(36) not null unique key,
    userKey varchar(36) not null unique key,
    userToken varchar(255) unique key,
    userTokenExpiresOn timestamp,
<<<<<<< HEAD
    storeUrl varchar(255) not null unique key,
    secureStoreUrl varchar(255) not null unique key,
    companyName varchar(255) not null unique key,
=======
    storeUrl varchar(255) unique key,
    secureStoreUrl varchar(255) unique key,
    companyName varchar(255) unique key,
>>>>>>> WIP Network Solutions commerce integration
    partnerId varchar(36) unique key
);


--//@UNDO
-- SQL to undo the change goes here.

drop table NetworkSolutionsPartner;
