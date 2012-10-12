--// Add_metrics_columns_to_Story
-- Migration SQL that makes the change goes here.

alter table Story
    add upVotes integer default 0,
    add downVotes integer default 0;

--//@UNDO
-- SQL to undo the change goes here.

alter table Story
    drop upVotes,
    drop downVotes;


