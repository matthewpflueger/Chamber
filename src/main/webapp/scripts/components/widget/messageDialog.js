define(
    [
        'jquery',
        'backbone',
        'underscore',
        'easyXDM'
    ],
    function($, Backbone, _, easyXDM){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                var self = this;
                this.element = $(this.el);
                this.socket = new easyXDM.Socket({
                    remote: this.options.properties.urls.api + "/widget/iframe/preview?pid=" + this.options.properties.partnerId,
                    container: this.element.attr("id"),
                    props: {
                        id: 'echoed-preview-iframe'
                    },
                    onReady: function(){
                        self.previewHidden = true;
                        self.socket.postMessage(JSON.stringify({ type: "text", data: "Click Here To Share Your DIYs"}));
                    },
                    onMessage: function(message, origin){
                        self.element.fadeIn();
                    }
                });
            },
            hide: function(){

            },
            show: function(){

            }
        });
    }
);
