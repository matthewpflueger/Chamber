--// Added CouponCode Columns to PartnerSettings
-- Migration SQL that makes the change goes here.

alter table PartnerSettings
        add column couponCode varchar(36) null,
        add column couponDescription varchar(1024) null,
        add column couponExpiresOn timestamp default '1970-01-01 00:00:00';

--//@UNDO
-- SQL to undo the change goes here.

alter table PartnerSettings
        drop couponCode,
        drop couponDescription,
        drop couponExpiresOn;



