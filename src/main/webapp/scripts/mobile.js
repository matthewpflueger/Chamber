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
        'components/infiniteScroll',
        'mobileConfig',
        'jqueryUI',
        'jqueryMobile'
    ],
    function(require, $, _, Backbone, Router, Exhibit, Story, Login, MessageHandler, InfiniteScroll){
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
            this.infiniteScroll = new InfiniteScroll({ el: "#infiniteScroll", EvAg: EventAggregator, properties: properties });
            Backbone.history.start();
        });
    }
);