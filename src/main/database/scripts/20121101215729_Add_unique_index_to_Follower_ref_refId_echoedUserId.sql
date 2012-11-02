--// Add_unique_index_to_Follower_ref_refId_echoedUserId
-- Migration SQL that makes the change goes here.

create unique index ref_refId_echoedUserId on Follower(ref, refId, echoedUserId);

--//@UNDO
-- SQL to undo the change goes here.

drop index ref_refId_echoedUserId on Follower;

