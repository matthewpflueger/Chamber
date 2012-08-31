--// Add_unique_index_to_TwitterFollower
-- Migration SQL that makes the change goes here.

create unique index twitterUserId_2 on TwitterFollower(twitterUserId, twitterId);

--//@UNDO
-- SQL to undo the change goes here.


