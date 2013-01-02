require(
    [
        'requireLib',
        'backbone',
        'underscore',
        'components/widget/overlay',
        'components/preview/background'
    ],
    function(require, Backbone, _, Overlay, Background){
        var self = this;
        this.EventAggregator = _.extend({}, Backbone.Events);
        this.properties = {
            urls: Echoed.urls,
            partnerId: Echoed.partnerId,
            isPreview: true,
            redirect: Echoed.redirect
        };
        this.overlay = new Overlay({ properties: self.properties, EvAg: self.EventAggregator });
        this.background = new Background({ el: "#background", properties: self.properties, EvAg: self.EventAggregator });
    }
);
