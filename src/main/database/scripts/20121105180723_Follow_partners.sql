--// Follow_partners
-- Migration SQL that makes the change goes here

insert into Follower(
    id,
    createdOn,
    updatedOn,
    echoedUserId,
    ref,
    refId)
select
    distinct uuid(),
    utc_timestamp() + 0,
    utc_timestamp() + 0,
    S.echoedUserId,
    'Partner',
    S.partnerId
from Story S
join Partner P on P.id = S.partnerId
where P.name != 'Echoed';

--//@UNDO
-- SQL to undo the change goes here.

delete from Follower where ref = 'Partner';
