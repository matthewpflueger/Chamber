require(
    [
        'requireLib',
        'jquery',
        'backbone',
        'underscore',
        'components/widget/remote',
        'components/widget/overlay',
        'components/widget/gallery',
        'components/widget/opener'
    ],
    function(require, $, Backbone, _,  Remote, Overlay, Gallery, Opener){
        var self = this;
        self.EventAggregator = _.extend({}, Backbone.Events);
        var scriptUrl = "";

        function gup(a){
            var b = a.split("?");
            if(b.length === 0) return {};

            else {
                var c = b[1].split("&");
                var d = {};
                for(var i = 0; i < c.length; i++){
                    var e = c[i].split("=");
                    d[e[0]] = e[1];
                }
                return d;
            }
        }

        if($('script[data-main*="loader.js"]').length > 0) scriptUrl = $('script[data-main*="loader.js"]').attr("data-main");
        else if($('script[src*="loader.js"]').length > 0) scriptUrl = $('script[src*="loader.js"]').attr('src');

        var parameters = gup(scriptUrl);

        var body = $('body');
        EchoedSettings.useOpener = true;
        self.properties = EchoedSettings;
        self.properties.partnerId = parameters['pid'];

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

        if(self.properties.useRemote === false && self.properties.useGallery === false) self.properties.useOpener = true;
        this.opener = new Opener({ el: "body", properties: self.properties, EvAg: self.EventAggregator });
    }
);