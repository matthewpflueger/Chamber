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
                var self = this;
                this.element = $(this.el);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.EvAg.bind("preview/show", this.showInfo);
                this.EvAg.bind("preview/leave", this.leave);

                this.socket = new easyXDM.Socket({
                    remote: this.options.properties.urls.api + "/widget/iframe/preview?pid=" + this.options.properties.partnerId,
                    container: this.element.attr("id"),
                    props: {
                        id: 'echoed-preview-iframe'
                    },
                    onReady: function(){
                        self.previewHidden = true;
                        self.showInfo({ type: "text", data: self.properties.widgetTitle });
                        self.leave();
                    },
                    onMessage: function(message, origin){
                        self.element.fadeIn();
                    }
                });
            },
            leave: function(){
                var self = this;
                self.previewHidden = true;
                window.setTimeout(function(){
                    if(self.previewHidden === true){
                        self.element.fadeOut();
                    }
                }, 1500);
            },
            showInfo: function(msgObj){
                this.previewHidden = false;
                this.socket.postMessage(JSON.stringify(msgObj));
            },
            hide: function(){

            },
            show: function(){

            }
        });
    }
);
