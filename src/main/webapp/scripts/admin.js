require(
    [
        'requireLib',
        'jquery',
        'backbone',
        'underscore',
        'routers/admin',
        'components/errorLog',
        'components/admin/moderate',
        'components/fade',
        'components/story',
        'components/admin/partnerList',
        'components/admin/echoedUsers'
    ],
    function(require, $, Backbone, _, Router, ErrorLog, Moderate, Fade, Story, PartnerList, EchoedUsers){
        $(document).ready(function(){
            var EventAggregator = _.extend({}, Backbone.Events);

            var properties = {
                urls: Echoed.urls
            };

            this.errorLog = new ErrorLog({ EvAg: EventAggregator, properties: properties });
            this.router = new Router({ EvAg: EventAggregator, properties: properties });
            this.moderate = new Moderate({ el: "#moderate", EvAg: EventAggregator, properties: properties });
            this.fade = new Fade({ el: "#fade", EvAg: EventAggregator, properties: properties });
            this.story = new Story({ el: "#story", EvAg: EventAggregator, properties: properties });
            this.partners = new PartnerList({ el: "#partnerList", EvAg: EventAggregator, properties: properties });
            this.echoedUsers = new EchoedUsers({ el: "#echoedUsers", EvAg: EventAggregator, properties: properties });
            Backbone.history.start();
        });
    }
);

