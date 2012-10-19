--// Add_emailVerified_field_to_EchoedUser
-- Migration SQL that makes the change goes here.

alter table EchoedUser
    add emailVerified boolean not null default false;

--//@UNDO
-- SQL to undo the change goes here.

alter table EchoedUser
    drop emailVerified;
