--// Drop_apiUser_index_on_Partner_tables
-- Migration SQL that makes the change goes here.

-- Drop the unique index
alter table BigCommercePartner drop index apiUser;
alter table BigCommercePartner drop index apiToken;

alter table NetworkSolutionsPartner drop index userKey;
alter table NetworkSolutionsPartner drop index userToken;

-- Add back a non-unique indexex
alter table BigCommercePartner add index apiUser (apiUser);
alter table NetworkSolutionsPartner add index userKey (userKey);


--//@UNDO
-- SQL to undo the change goes here.

alter table BigCommercePartner add index apiToken (apiToken);
alter table NetworkSolutionsPartner add index userToken (userToken);


