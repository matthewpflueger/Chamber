define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'easyXDM',
        'json2'
    ],
    function($, Backbone, _, utils, easyXDM, JSON2){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                var self = this;
                this.properties = options.properties;
                this.element =$(this.el);

                this.socket = new easyXDM.Socket({
                    remote: this.properties.urls.api +  "/widget/iframe/gallery?pid=" + this.properties.partnerId,
                    container: "echoed-gallery",
                    props: { id: "echoed-gallery-iframe" },
                    onMessage: function(message, origin){
                        var msg = JSON.parse(message);
                        switch(msg.type){
                            case 'load':
                                window.location.hash = "#echoed_" + msg.data ;
                                break;
                            case 'resize':
                                //$('#echoed-gallery-iframe').hide().height(msg.data).slideDown();
                                $('#echoed-gallery-iframe').animate({
                                    height: msg.data
                                });
                                break;
                            case 'close':
                                self.element.slideUp();
                        }
                    }
                });
            }
        });
    }
);