require(
    [
        'requireLib',
        'backbone',
        'underscore',
        'components/widget/overlay'
    ],
    function(require, Backbone, _, Overlay){
        var self = this;
        this.EventAggregator = _.extend({}, Backbone.Events);
        this.properties = {
            urls: Echoed.urls,
            partnerId: Echoed.partnerId,
            isPreview: true,
            redirect: Echoed.redirect
        };
        this.overlay = new Overlay({ properties: self.properties, EvAg: self.EventAggregator });
    }
);
