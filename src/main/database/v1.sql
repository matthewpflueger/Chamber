drop database Echoed;
create database Echoed default character set utf8;
use Echoed;



drop table if exists Retailer;
create table Retailer (
    id varchar(36) not null,
    primary key(id)
) engine = InnoDB;

drop table if exists Echo;
create table Echo (
    id varchar(36) not null,
    retailerId varchar(36) not null,
    customerId varchar(255) not null,
    productId varchar(255) not null,
    boughtOn datetime not null,
    orderId varchar(255) not null,
    price varchar(255) not null,
    imageUrl varchar(1024) not null,
    echoedUserId varchar(36) not null,
    facebookPostId varchar(36),
    twitterStatusId varchar(36),
    echoPossibilityId varchar(255) not null,
    landingPageUrl varchar(1024) not null,
    primary key(id),
    unique key(facebookPostId),
    unique key(echoPossibilityId)
) engine = InnoDB;

drop table if exists EchoClick;
create table EchoClick (
    id varchar(36) not null,
    echoId varchar(36) not null,
    facebookPostId varchar(36),
    twitterStatusId varchar(36),
    echoedUserId varchar(36),
    referrerUrl varchar(1024) not null,
    ipAddress varchar(36) not null,
    clickedOn datetime not null,
    primary key(id)
) engine = MyIsam;

drop table if exists FacebookPost;
create table FacebookPost (
    id varchar(36) not null,
    message varchar(255) not null,
    picture varchar(1024) not null,
    link varchar(1024) not null,
    facebookUserId varchar(36) not null,
    echoedUserId varchar(36) not null,
    echoId varchar(36) not null,
    postedOn datetime,
    createdOn datetime not null,
    objectId varchar(255),
    primary key(id),
    unique key(echoId)
) engine = InnoDB;

drop table if exists EchoPossibility;
create table EchoPossibility (
    id varchar(255) not null,
    retailerId varchar(36) not null,
    customerId varchar(255) not null,
    productId varchar(255) not null,
    boughtOn datetime not null,
    step varchar(36) not null,
    orderId varchar(255) not null,
    price varchar(255) not null,
    imageUrl varchar(1024) not null,
    echoedUserId varchar(36),
    echoId varchar(36),
    landingPageUrl varchar(1024),
    primary key(id),
    unique key(echoId)
) engine = InnoDB;


drop table if exists EchoedUser;
create table EchoedUser (
	id varchar(36) not null,
	username varchar(255),
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
	username varchar(36),
	firstName varchar(255) not null,
	lastName varchar(255) not null,
	email varchar(255) not null,
	link varchar(1024) not null,
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

drop table if exists TwitterStatus;
create table TwitterStatus(
    id varchar(255) not null,
    twitterId varchar(255) not null,
    twitterUserId varchar(255) not null,
    text varchar(255) not null,
    createdAt date,
    source varchar(255),
    primary key(id, twitterId)
) engine = InnoDB;

drop table if exists DatabaseVersion;
create table DatabaseVersion (
    id integer auto_increment not null,
    updatedOn timestamp not null,
    primary key(id)
) engine = MyISAM;


