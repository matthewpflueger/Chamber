select
    EU.name,
    FP.id,
    FP.crawledOn,
    FP.crawledStatus,
    FC.name as commentBy,
    FC.message,
    FL.name as likedBy
from FacebookPost FP
    join EchoedUser EU on EU.id = FP.echoedUserId
    join FacebookComment FC on FC.echoedUserId = EU.id
    left join FacebookLike FL on FL.echoedUserId = EU.id
where EU.name like '%Vanes%';

select FC.* from FacebookComment FC
join EchoedUser EU on EU.facebookId = FC.facebookId
join Echo E on E.echoedUserId = EU.id;
