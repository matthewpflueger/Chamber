require(
    [
        'requireLib',
        'jquery',
        'backbone',
        'underscore',
        'routers/admin',
        'components/errorLog',
        'components/admin/moderate',
        'components/story',
        'components/admin/partnerList',
        'components/admin/echoedUsers',
        'models/user'
    ],
    function(require, $, Backbone, _, Router, ErrorLog, Moderate, Story, PartnerList, EchoedUsers, ModelUser){
        $(document).ready(function(){
            var EventAggregator = _.extend({}, Backbone.Events);

            var properties = {
                urls: Echoed.urls
            };

            this.modelUser = new ModelUser(null, { properties: properties });
            this.errorLog = new ErrorLog({ EvAg: EventAggregator, properties: properties });
            this.router = new Router({ EvAg: EventAggregator, properties: properties });
            this.moderate = new Moderate({ el: "#moderate", EvAg: EventAggregator, properties: properties });
            this.story = new Story({ el: '#story-container', EvAg: EventAggregator, properties: properties, modelUser: this.modelUser});
            this.partners = new PartnerList({ el: "#partnerList", EvAg: EventAggregator, properties: properties });
            this.echoedUsers = new EchoedUsers({ el: "#echoedUsers", EvAg: EventAggregator, properties: properties });
            Backbone.history.start();
        });
    }
);

