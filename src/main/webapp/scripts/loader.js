require(
    [
        'requireLib',
        'jquery',
        'backbone',
        'underscore',
        'components/widget/remote',
        'components/widget/overlay',
        'components/widget/iframe.gallery',
        'components/widget/opener'
    ],
    function(require, $, Backbone, _,  Remote, Overlay, Gallery, Opener){
        var self = this;
        self.EventAggregator = _.extend({}, Backbone.Events);

        var body = $(document.body);
        EchoedSettings.useOpener = true;
        self.properties = EchoedSettings;
        body.append($('<link rel="stylesheet" type="text/css"/>').attr("href", self.properties.urls.css + "/remote.css"));

        this.overlay = new Overlay({ properties: self.properties, EvAg: self.EventAggregator });


        if(self.properties.useRemote){
            $('<div id="echoed-loader"></div>').appendTo(body);
            this.remote = new Remote({ el: "#echoed-loader", properties: self.properties, EvAg: self.EventAggregator});
        }

        if(self.properties.useGallery){
            $('<div id="echoed-gallery"></div>').appendTo(body);
            this.gallery = new Gallery({ el: "#echoed-gallery", properties: self.properties, EvAg: self.EventAggregator });
        }

        if(self.properties.useRemote === true || self.properties.useGallery === true) self.properties.useOpener = false;
        this.opener = new Opener({ el: "body", properties: self.properties, EvAg: self.EventAggregator });
    }
);