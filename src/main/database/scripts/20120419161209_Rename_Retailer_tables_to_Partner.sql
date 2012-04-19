--// Rename Retailer tables to Partner
-- Migration SQL that makes the change goes here.

alter table RetailerSettings
        change retailerId partnerId varchar(36) not null,
        drop index retailerId,
        drop index retailerId_2,
        add index partnerId(partnerId),
        add unique index partnerId_2(partnerId, activeOn);

alter table RetailerUser
        change retailerId partnerId varchar(36) not null,
        drop index retailerId,
        add index partnerId(partnerId);

alter table Echo
        change retailerId partnerId varchar(36) not null,
        change retailerSettingsId partnerSettingsId varchar(36) not null,
        drop index retailerId,
        add index partnerId(partnerId);

alter table EchoMetrics
        change retailerId partnerId varchar(36) not null,
        change retailerSettingsId partnerSettingsId varchar(36) not null,
        drop index retailerId,
        add index partnerId(partnerId),
        drop index retailerSettingsId,
        add index partnerSettingsId(partnerSettingsId);;

alter table Retailer rename Partner;

alter table RetailerSettings rename PartnerSettings;

alter table RetailerUser rename PartnerUser;


--//@UNDO
-- SQL to undo the change goes here.


alter table Partner rename Retailer;

alter table PartnerSettings rename RetailerSettings;

alter table PartnerUser rename RetailerUser;


alter table RetailerSettings
        change partnerId retailerId varchar(36) not null,
        drop index partnerId,
        drop index partnerId_2,
        add index retailerId(retailerId),
        add unique index retailerId_2(retailerId, activeOn);


alter table RetailerUser
        change partnerid retailerId varchar(36) not null,
        drop index partnerId,
        add index retailerId(retailerId);


alter table Echo
        change partnerId retailerId varchar(36) not null,
        change partnerSettingsId retailerSettingsId varchar(36) not null,
        drop index partnerId,
        add index retailerId(retailerId);


alter table EchoMetrics
        change partnerId retailerId varchar(36) not null,
        change partnerSettingsId retailerSettingsId varchar(36) not null,
        drop index partnerId,
        add index retailerId(retailerId),
        drop index partnerSettingsId,
        add index retailerSettingsId(retailerSettingsId);

