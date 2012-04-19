--// Add views to PartnerSettings
-- Migration SQL that makes the change goes here.

alter table PartnerSettings
        add views varchar(255) not null;

update PartnerSettings set views = "echo.js.0,echo.js.1";

--//@UNDO
-- SQL to undo the change goes here.

alter table PartnerSettings
        drop views;


