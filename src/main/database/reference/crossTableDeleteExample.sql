-- The reference used: http://stackoverflow.com/questions/4743649/cant-get-join-on-mysql-delete-query-to-work
delete E, EM from Echo E join EchoMetrics EM on E.echoMetricsId = EM.id where E.landingPageUrl like '%test888%';

delete SP, PU, PS, P from ShopifyPartner SP
join PartnerUser PU on SP.partnerId = PU.partnerId
join PartnerSettings PS on SP.partnerId = PS.partnerId
join Partner P on SP.partnerId = P.id
where SP.partnerId = '4bd0df96-e461-4894-a25d-c84dae3176a';
