--// Add Category Column to Retailer
-- Migration SQL that makes the change goes here.

alter table Retailer
    add category varchar(255) not null;

update Retailer
    set category = 'Other'
    where category = '';


--//@UNDO
-- SQL to undo the change goes here.

alter table Retailer
    drop category;


