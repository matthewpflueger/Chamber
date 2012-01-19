--// Bootstrap.sql

-- This is the only SQL script file that is NOT
-- a valid migration and will not be run or tracked
-- in the changelog.  There is no @UNDO section.

--// Do I need this file?

-- New projects likely won't need this file.
-- Existing projects will likely need this file.
-- It's unlikely that this bootstrap should be run
-- in the production environment.

--// Purpose

-- The purpose of this file is to provide a facility
-- to initialize the database to a state before MyBatis
-- SQL migrations were applied.  If you already have
-- a database in production, then you probably have
-- a script that you run on your developer machine
-- to initialize the database.  That script can now
-- be put in this bootstrap file (but does not have
-- to be if you are comfortable with your current process.

--// Running

-- The bootstrap SQL is run with the "migrate bootstrap"
-- command.  It must be run manually, it's never run as
-- part of the regular migration process and will never
-- be undone. Variables (e.g. ${variable}) are still
-- parsed in the bootstrap SQL.

-- After the boostrap SQL has been run, you can then
-- use the migrations and the changelog for all future
-- database change management.



drop database Echoed;
create database Echoed default character set utf8;
use Echoed;

-- NOTE: updatedOn must be the first timestamp column as it will be auto-updated
--       see http://dev.mysql.com/doc/refman/5.1/en/timestamp.html
--


drop table if exists Retailer;
create table Retailer (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    name varchar(255) not null unique key
);

drop table if exists RetailerSettings;
create table RetailerSettings (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    retailerId varchar(36) not null,
    closetPercentage float not null,
    minClicks integer not null,
    minPercentage float not null,
    maxClicks integer not null,
    maxPercentage float not null,
    echoedMatchPercentage float not null,
    echoedMaxPercentage float not null,
    activeOn timestamp not null,
    unique key(retailerId, activeOn),
    index (activeOn desc),
    index (retailerId)
);

drop table if exists RetailerUser;
create table RetailerUser (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    retailerId varchar(36) not null,
    name varchar(255) not null,
    email varchar(255) not null unique key,
    salt varchar(255) not null unique key,
    password varchar(255) not null unique key,
    index (retailerId)
);

drop table if exists Echo;
create table Echo (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    retailerId varchar(36) not null,
    customerId varchar(255) not null,
    productId varchar(255) not null,
    boughtOn timestamp not null,
    orderId varchar(255) not null,
    price float not null,
    imageUrl varchar(1024) not null,
    echoedUserId varchar(36) not null,
    facebookPostId varchar(36) unique key,
    twitterStatusId varchar(36) unique key,
    echoPossibilityId varchar(255) not null unique key,
    landingPageUrl varchar(1024) not null,
    retailerSettingsId varchar(36) not null,
    totalClicks integer not null,
    credit float not null,
    fee float not null,
    productName varchar(255),
    category varchar(255),
    brand varchar(255),
    index (retailerId),
    index (echoedUserId)
);

drop table if exists EchoClick;
create table EchoClick (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    echoId varchar(36) not null,
    facebookPostId varchar(36),
    twitterStatusId varchar(36),
    echoedUserId varchar(36),
    referrerUrl varchar(1024) not null,
    ipAddress varchar(36) not null,
    clickedOn timestamp not null,
    index (echoId),
    index (facebookPostId),
    index (twitterStatusId),
    index (echoedUserId)
);

drop table if exists FacebookPost;
create table FacebookPost (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    message varchar(255) not null,
    picture varchar(1024) not null,
    link varchar(1024) not null,
    facebookUserId varchar(36) not null,
    echoedUserId varchar(36) not null,
    echoId varchar(36) not null unique key,
    postedOn timestamp null,
    facebookId varchar(255) unique key,
    crawledStatus varchar(255),
    crawledOn timestamp null,
    index (postedOn),
    index (facebookId),
    index (facebookUserId),
    index (echoedUserId),
    index (crawledOn)
);

drop table if exists FacebookLike;
create table FacebookLike(
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    facebookPostId varchar(36) not null,
    facebookUserId varchar(36) not null,
    echoedUserId varchar(36) not null,
    facebookId varchar(255) not null,
    name varchar(255) not null,
    unique key(facebookPostId, facebookId),
    index(facebookPostId),
    index(facebookUserId)
);

drop table if exists FacebookComment;
create table FacebookComment(
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    facebookPostId varchar(36) not null,
    facebookUserId varchar(36) not null,
    echoedUserId varchar(36) not null,
    facebookId varchar(255) not null unique key,
    byFacebookId varchar(255) not null,
    name varchar(255) not null,
    message varchar(255) not null,
    createdAt timestamp not null,
    index(facebookPostId),
    index(facebookUserId)
);

drop table if exists EchoPossibility;
create table EchoPossibility (
    id varchar(255) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    retailerId varchar(36) not null,
    customerId varchar(255) not null,
    productId varchar(255) not null,
    boughtOn timestamp not null,
    step varchar(36) not null,
    orderId varchar(255) not null,
    price float not null,
    imageUrl varchar(1024) not null,
    echoedUserId varchar(36),
    echoId varchar(36) unique key,
    landingPageUrl varchar(1024),
    productName varchar(255),
    category varchar(255) ,
    brand varchar (255) ,
    index(retailerId)
);

drop table if exists EchoedUser;
create table EchoedUser (
	id varchar(36) not null primary key,
	updatedOn timestamp not null,
	createdOn timestamp not null,
	name varchar(255) not null,
	email varchar(255) unique key,
	screenName varchar(255) unique key,
	facebookUserId varchar(36) unique key,
	facebookId varchar(36) unique key,
	twitterUserId varchar(36) unique key,
	twitterId varchar(36) unique key
);

drop table if exists EchoedFriend;
create table EchoedFriend (
	id varchar(36) not null primary key,
	updatedOn timestamp not null,
	createdOn timestamp not null,
	fromEchoedUserId varchar(36) not null,
	toEchoedUserId varchar(36) not null,
	name varchar(255),
	screenName varchar(255),
	facebookUserId varchar(36),
	facebookId varchar(36),
	twitterUserId varchar(36),
	twitterId varchar(36),
	unique key(fromEchoedUserId, toEchoedUserId),
	index(fromEchoedUserId),
	index(toEchoedUserId)
);

drop table if exists TwitterUser;
create table TwitterUser(
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    echoedUserId varchar(36) unique key,
    twitterId varchar(255) not null unique key,
    screenName varchar(36) not null unique key,
    name varchar(255) not null,
    profileImageUrl varchar(255) not null,
    location varchar(36),
    timezone varchar(36),
    accessToken varchar(255) not null,
    accessTokenSecret varchar(255) not null
);

drop table if exists TwitterFollower;
create table TwitterFollower(
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    twitterUserId varchar(36) not null,
    twitterId varchar(255) not null,
    name varchar(255),
    index (twitterUserId),
    index (twitterId)
);

drop table if exists FacebookUser;
create table FacebookUser (
	id varchar(36) not null primary key,
	updatedOn timestamp not null,
	createdOn timestamp not null,
	echoedUserId varchar(36) unique key,
	facebookId varchar(255) not null unique key,
	name varchar(255) not null,
	email varchar(255) not null unique key,
	link varchar(1024) not null,
	timezone varchar(36) not null,
	locale varchar(36) not null,
	gender varchar(36) not null,
	accessToken varchar(255) not null unique key
);

drop table if exists FacebookTestUser;
create table FacebookTestUser (
	id varchar(36) not null primary key,
	updatedOn timestamp not null,
	createdOn timestamp not null,
	echoedUserId varchar(36),
	facebookUserId varchar(36),
	facebookId varchar(255) not null unique key,
	name varchar(255),
	email varchar(255),
	password varchar(255),
	loginUrl varchar(255),
	accessToken varchar(255),
	index(echoedUserId),
	index(facebookUserId),
	index(email)
);

drop table if exists FacebookFriend;
create table FacebookFriend (
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    facebookUserId varchar(36) not null,
    facebookId varchar(255) not null,
    name varchar(255) not null,
    unique key(facebookUserId, facebookId),
    index (facebookUserId),
    index (facebookId)
);

drop table if exists TwitterStatus;
create table TwitterStatus(
    id varchar(36) not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null,
    echoId varchar(36) not null,
    echoedUserId varchar(36) not null,
    message varchar(255) not null,
    twitterId varchar(255),
    createdAt timestamp null,
    text varchar(255),
    source varchar(255),
    postedOn timestamp null,
    index (echoId),
    index (echoedUserId)
);

drop table if exists DatabaseVersion;
create table DatabaseVersion (
    id integer auto_increment not null primary key,
    updatedOn timestamp not null,
    createdOn timestamp not null
);

