-- The reference used: http://stackoverflow.com/questions/4743649/cant-get-join-on-mysql-delete-query-to-work
delete E, EM from Echo E join EchoMetrics EM on E.echoMetricsId = EM.id where E.landingPageUrl like '%test888%';
