drop database Echoed;
create database Echoed default character set utf8;
use Echoed;



drop table if exists Retailer;
create table Retailer (
    id varchar(36) not null,
    primary key(id)
) engine = InnoDB;

drop table if exists EchoPossibility;
create table EchoPossibility (
    id varchar(255) not null,
    retailerId varchar(36) not null,
    customerId varchar(255) not null,
    productId varchar(255) not null,
    boughtOn timestamp not null,
    step varchar(36) not null,
    echoedUserId varchar(36),
    primary key(id)
) engine = InnoDB;


drop table if exists EchoedUser;
create table EchoedUser (
	id varchar(36) not null,
	username varchar(255) not null,
	email varchar(255) not null,
	firstName varchar(255) not null,
	lastName varchar(255) not null,
	facebookUserId varchar(36),
	twitterUserId varchar(36),
	primary key(id),
    unique key(username),
	unique key(email),
	unique key(facebookUserId),
    unique key(twitterUserId)
) engine = InnoDB;

drop table if exists TwitterUser;
create table TwitterUser(
  id varchar(36) not null,
  echoedUserId varchar(36),
  username varchar(36) not null,
  name varchar(36) not null,
  location varchar(36) null,
  timezone varchar(36) null,
  accessToken varchar(255) not null,
  accessTokenSecret varchar(255) not null,
  primary key(id)
) engine = InnoDB;

drop table if exists FacebookUser;
create table FacebookUser (
	id varchar(36) not null,
	echoedUserId varchar(36),
	username varchar(36) not null,
	firstName varchar(255) not null,
	lastName varchar(255) not null,
	email varchar(255) not null,
	link varchar(255) not null,
	timezone varchar(36) not null,
	locale varchar(36) not null,
	gender varchar(36) not null,
	accessToken varchar(255) not null,
	primary key(id),
	unique key(email),
	unique key(accessToken)
) engine = InnoDB;

drop table if exists FacebookFriend;
create table FacebookFriend (
    id varchar(36) not null,
    facebookUserId varchar(36) not null,
    name varchar(255) not null,
    primary key(id, facebookUserId)
) engine = InnoDB;

drop table if exists DatabaseVersion;
create table DatabaseVersion (
    id integer auto_increment not null,
    updateOn timestamp not null,
    primary key(id)
) engine = MyISAM;