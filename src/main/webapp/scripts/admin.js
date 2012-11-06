require.config({
    paths: {
        'backbone': 'libs/backbone-amd-0.9.2-min',
        'underscore': 'libs/underscore-amd-1.4.1-min',
        'jquery': 'libs/jquery-1.8.1.min',
        'isotope': 'libs/jquery.isotope.min',
        'expanding' : 'libs/expanding',
        'fileuploader': 'libs/fileuploader',
        'text': 'libs/require/text',
        'requireLib': 'libs/require/require'
    },
    shim: {
        fileuploader: {
            exports: 'qq'
        }
    }
});


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

