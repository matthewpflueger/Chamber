require(
    [
        'requireLib',
        'jquery',
        'backbone',
        'underscore',
        'components/widget/overlay',
        'components/preview/background'
    ],
    function(require, $, Backbone, _, Overlay, Background){

        this.EventAggregator = _.extend({}, Backbone.Events);

        var overlayUrl = Echoed.urls.site + "/app/iframe";
        if(Echoed.partnerId !== "") overlayUrl += "#partner/" + Echoed.partnerId;

        this.properties = {
            urls:       Echoed.urls,
            partnerId:  Echoed.partnerId,
            isPreview:  true,
            overlayUrl: overlayUrl
        };

        if(Echoed.redirect) this.properties.redirect = Echoed.redirect
        else this.properties.redirect = window.location;

        this.overlay =      new Overlay({ properties: this.properties, EvAg: this.EventAggregator, showOverlay: true });
        var background = document.getElementById("echoed-background");
        if(!background) {
            background = document.createElement("iframe");
            background.id = "echoed-background";
            var body = document.getElementsByTagName("body")[0];
            body.appendChild(background);
            background.style.cssText = "position:fixed; top:0px; left:px; right: 0px; bottom: 0px; height: 100%; width: 100%; z-index: 9000000";
        }
        this.background =   new Background({ el: "#echoed-background", properties: this.properties, EvAg: this.EventAggregator });
    }
);
