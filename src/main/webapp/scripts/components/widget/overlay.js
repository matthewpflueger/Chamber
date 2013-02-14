define(
    [
        'jquery',
        'backbone',
        'underscore',
        'easyXDM',
        'json2'
    ],
    function($, Backbone, _, easyXDM, JSON){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                var self =          this;
                this.EvAg =         options.EvAg;
                this.properties =   options.properties;
                this.htmlEl =       $('html');

                this.socket = new easyXDM.Socket({
                    remote: this.properties.overlayUrl,
                    props: {
                        id: "echoed-overlay"
                    },
                    onReady: function(){
                        self.element = $('#echoed-overlay');
                        self.element.removeAttr('style');
                        if(options.showOverlay){
                            self.element.css({
                                position:   "fixed",
                                top:        "0px",
                                left:       "0px",
                                bottom:     "0px",
                                right:      "0px",
                                height:     "100%",
                                width:      "100%",
                                "z-index":    "99999999999999"
                            });
                        }
                        if(self.properties.isPreview){
                            self.showOverlay();
                            self.EvAg.trigger("background/show");
                        } else {
                            self.parseHash();
                            window.onhashchange = self.parseHash;
                        }
                    },
                    onMessage: function(message, origin){
                        self.handleMessage(message, origin);
                    }
                });
                this.EvAg.bind('overlay/show', this.showOverlay);
            },
            handleMessage: function(message, origin){
                try{
                    var msgObj = JSON.parse(message);
                    switch(msgObj.type){
                        case "close":
                            if(self.properties.isPreview){
                                window.location = this.properties.redirect;
                            } else {
                                var hash = window.location.hash;
                                var index = hash.indexOf('echoed');
                                if(index > 0) window.location.hash = hash.substr(0, index);
                                this.hideOverlay();
                            }
                            break;
                        case "contextChange":
                            var p = "http://";
                            if (msgObj.data.substring(0, p.length) !== p) {
                                msgObj.data = p + msgObj.data;
                            }
                            this.EvAg.trigger("background/update", msgObj.data);
                            this.properties.redirect = msgObj.data;
                    }
                } catch(e){

                }
            },
            hideOverlay: function(){
                var self = this;
                this.element.fadeOut(function(){
                    self.htmlEl.css({"overflow": "auto" });
                });
            },
            showOverlay: function(type, data){
                type = typeof type == 'undefined' ? "hash" : type;
                data = typeof data == 'undefined' ? "#home" : data;
                this.htmlEl.css({ "overflow" : "hidden" });
                this.element.fadeIn();
                this.socket.postMessage(JSON.stringify({ "type":  type, "data": data}));
            },
            parseHash: function(){
                var hash = window.location.hash;
                var index = hash.indexOf('echoed');
                if(index > 0){
                    var iFrameHash = '#home';
                    var endPoint = hash.substr(index).split('_')[1];
                    if(endPoint) iFrameHash = '#' + endPoint;
                    this.showOverlay('hash', iFrameHash);
                }
            }
        });
    }
);