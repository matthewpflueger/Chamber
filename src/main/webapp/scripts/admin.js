require(
    [
        'requireLib',
        'jquery',
        'backbone',
        'underscore',
        'routers/admin',
        'components/errorLog',
        'components/admin/moderate',
        'components/admin/partnerList',
        'components/admin/echoedUsers',
        'models/user',
        'views/item/item'
    ],
    function(require, $, Backbone, _, Router, ErrorLog, Moderate, PartnerList, EchoedUsers, ModelUser, Item) {
        $(document).ready(function(){
            var EventAggregator = _.extend({}, Backbone.Events);

            var properties = {
                urls: Echoed.urls
            };

            this.modelUser = new ModelUser(null, { properties: properties });
            this.errorLog = new ErrorLog({ EvAg: EventAggregator, properties: properties });
            this.router = new Router({ EvAg: EventAggregator, properties: properties });
            this.moderate = new Moderate({ el: "#moderate", EvAg: EventAggregator, properties: properties });
            this.item = new Item({ el: '#item-container', EvAg: EventAggregator, properties: properties, modelUser: this.modelUser});
            this.partners = new PartnerList({ el: "#partnerList", EvAg: EventAggregator, properties: properties });
            this.echoedUsers = new EchoedUsers({ el: "#echoedUsers", EvAg: EventAggregator, properties: properties });
            Backbone.history.start();
        });
    }
);

