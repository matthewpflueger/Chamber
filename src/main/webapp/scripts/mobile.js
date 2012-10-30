require.config({
    paths: {
        'backbone': 'libs/backbone-amd-0.9.2-min',
        'underscore': 'libs/underscore-amd-1.4.1-min',
        'jquery': 'libs/jquery-1.8.1.min',
        'jqueryMobile': 'libs/jquery.mobile-1.1.1.min',
        'jqueryMobileConfig': 'libs/jquery.mobile.config',
        'isotope': 'libs/jquery.isotope.min',
        'expanding' : 'libs/expanding',
        'fileuploader': 'libs/fileuploader',
        'text': 'libs/require/text',
        'requireLib': 'libs/require/require'
    },
    shim: {
        'jqueryMobileConfig': ['jquery'],
        'jqueryMobile': ['jquery', 'jqueryMobileConfig']
    }
});

require(
    [
        'requireLib',
        'jquery',
        'underscore',
        'backbone',
        'routers/mobile',
        'components/mobile/exhibit',
        'components/mobile/story',
        'components/mobile/login',
        'components/mobile/messageHandler',
        'jqueryMobile'
    ],
    function(require, $, _, Backbone, Router, Exhibit, Story, Login, MessageHandler){
        $(document).ready(function(){
            var properties = {
                urls: Echoed.urls,
                echoedUser: Echoed.echoedUser,
                exhibitShowLogin: true
            };

            var EventAggregator = _.extend({}, Backbone.Events);

            this.router = new Router({ EvAg: EventAggregator, properties: properties});
            this.exhibit = new Exhibit({ el: '#content', EvAg: EventAggregator, properties: properties });
            this.story = new Story({ el: '#story', EvAg: EventAggregator, properties: properties });
            this.login = new Login({ el: '#login', EvAg: EventAggregator, properties: properties });
            this.messageHandler = new MessageHandler({ EvAg: EventAggregator, properties: properties });
            Backbone.history.start();
        });
    }
);