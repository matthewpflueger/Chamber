require(
    [
        'requireLib',
        'backbone',
        'underscore',
        'components/widget/overlay',
        'components/preview/background'
    ],
    function(require, Backbone, _, Overlay, Background){

        this.EventAggregator = _.extend({}, Backbone.Events);

        this.properties = {
            urls:       Echoed.urls,
            partnerId:  Echoed.partnerId,
            isPreview:  true,
            redirect:   Echoed.redirect
        };
        this.overlay =      new Overlay({ properties: this.properties, EvAg: this.EventAggregator });
        this.background =   new Background({ el: "#background", properties: this.properties, EvAg: this.EventAggregator });
    }
);
