--// Change MyISAM tables to InnoDB
-- Migration SQL that makes the change goes here.

alter table AdminUser engine = InnoDB;
alter table Changelog engine = InnoDB;
alter table EchoMetrics engine = InnoDB;
alter table GeoLocation engine = InnoDB;
alter table Image engine = InnoDB;
alter table ShopifyPartner engine = InnoDB;

--//@UNDO
-- SQL to undo the change goes here.

alter table AdminUser engine = MyISAM;
alter table Changelog engine = MyISAM;
alter table EchoMetrics engine = MyISAM;
alter table GeoLocation engine = MyISAM;
alter table Image engine = MyISAM;
alter table ShopifyPartner engine = MyISAM;

