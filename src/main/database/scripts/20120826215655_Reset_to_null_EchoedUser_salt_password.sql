--// Reset_to_null_EchoedUser_salt_password
-- Migration SQL that makes the change goes here.

update EchoedUser set
    salt = null,
    password = null;

--//@UNDO
-- SQL to undo the change goes here.


