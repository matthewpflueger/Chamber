--// Add_password_salt_to_EchoedUser
-- Migration SQL that makes the change goes here.

alter table EchoedUser
    add column password varchar(255) null,
    add column salt varchar(255) null;

create unique index password on EchoedUser(password);
create unique index salt on EchoedUser(salt);

--//@UNDO
-- SQL to undo the change goes here.

alter table EchoedUser
    drop password,
    drop salt;
