select Month(createdOn), count(*) from Echo where echoedUserId is not null group by Month(createdOn);

select Month(createdOn), sum(price) from Echo where echoedUserId is not null group by Month(createdOn);

select Month(createdOn), sum(price) from Echo group by Month(createdOn);

select Date(createdOn), count(*) from EchoedUser group by Date(createdOn);

select Month(createdOn), count(*) from EchoedUser group by Month(createdOn);

select Month(createdOn), count(*) from EchoClick where filtered = 0 group by Month(createdOn);

select Month(createdOn), count(*) from Echo where echoClickId is not null group by Month(createdOn);

select Month(createdOn), count(*) from Echo where echoClickId is not null group by Month(createdOn);

select Month(Partner.createdOn), count(distinct Partner.id) from Partner join PartnerSettings on Partner.id = PartnerSettings.partnerId where activeOn < Now() and PartnerSettings.views not like '%free%' group by Month(createdOn);

select Month(Partner.createdOn), count(distinct Partner.id) from Partner join PartnerSettings on Partner.id = PartnerSettings.partnerId where activeOn < Now() and PartnerSettings.views not like '%free%' group by Month(createdOn);

select Month(E.createdOn), sum(E.price) from Echo E join PartnerSettings PS on PS.id=E.partnerSettingsId where PS.activeOn <= Now() group by Month(E.createdOn);

select Month(createdOn), count(*) from Partner group by Month(createdOn);
