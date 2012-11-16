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
                    partnerUser: Echoed.partnerUser,
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


