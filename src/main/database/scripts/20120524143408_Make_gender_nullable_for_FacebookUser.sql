--// Make_gender_nullable_for_FacebookUser
-- Migration SQL that makes the change goes here.

alter table FacebookUser
    modify gender varchar(36) null;

--//@UNDO
-- SQL to undo the change goes here.

-- not worrying about putting it back as non-null

