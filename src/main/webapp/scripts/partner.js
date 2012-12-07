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
        'components/partner/customize'
    ],
    function(require, $, _, Backbone, Router, ErrorLog, HowToInstall, Moderate, Story, Customize){

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
                this.customize = new Customize({ el: '#customize', EvAg: EventAggregator, properties: properties });
                Backbone.history.start();
            }
        );
    }
);


