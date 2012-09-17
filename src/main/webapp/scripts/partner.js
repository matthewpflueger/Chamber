require.config({
    paths: {
        'backbone': 'libs/backbone-amd-0.9.2-min',
        'underscore': 'libs/underscore-amd-1.3.3-min',
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
        'underscore',
        'backbone',
        'routers/partner',
        'components/errorLog',
        'components/partner/howToInstall',
        'components/partner/moderate',
        'components/story',
        'components/fade'
    ],
    function(require, $, _, Backbone, Router, ErrorLog, HowToInstall, Moderate, Story, Fade){

        $(document).ready(function(){
                var EventAggregator = _.extend({}, Backbone.Events);

                var properties = {
                    urls: Echoed.urls,
                    echoedUser: Echoed.echoedUser,
                    exhibitShowLogin: true
                };

                this.errorLog = new ErrorLog({ EvAg: EventAggregator, properties: properties });
                this.router = new Router({ EvAg: EventAggregator, properties: properties });
                this.howToInstall = new HowToInstall({ el: '#howToInstall', EvAg: EventAggregator, properties: properties });
                this.moderate = new Moderate({ el: '#moderate', EvAg: EventAggregator, properties: properties });
                this.story = new Story({ el: '#story', EvAg: EventAggregator, properties: properties });
                this.fade = new Fade({ el : '#fade', EvAg: EventAggregator, properties: properties });
                Backbone.history.start();
            }
        );
    }
);


