--// Add_Echoed_partner_and_default_settings
-- Migration SQL that makes the change goes here.

insert into Partner(
    id,
    updatedOn,
    createdOn,
    name,
    domain,
    handle,
    secret,
    category)
values(
    uuid(),
    now(),
    now(),
    'Echoed',
    'echoed.com',
    'Echoed',
    uuid(),
    'Other');

insert into PartnerSettings(
    id,
    updatedOn,
    createdOn,
    partnerId,
    closetPercentage,
    minClicks,
    minPercentage,
    maxClicks,
    maxPercentage,
    echoedMatchPercentage,
    echoedMaxPercentage,
    activeOn,
    creditWindow,
    views,
    hashtag,
    storyPrompts)
select
    uuid(),
    now(),
    now(),
    id,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    now(),
    0,
    'echo.js.free',
    '@Echoed',
    '{ "prompts": ["Why did you buy it?", "What are you doing with it?", "Who is it for?"] }'
from Partner
where handle = 'Echoed';


--//@UNDO
-- SQL to undo the change goes here.

delete PS, P from Partner P
join PartnerSettings PS on PS.partnerId = P.id
where P.handle = 'Echoed';

