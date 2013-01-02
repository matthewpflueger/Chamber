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
                this.EvAg = options.EvAg;
                this.EvAg.bind('msg/send', this.sendMessage);
                this.properties = options.properties;
                if(window.addEventListener){
                    window.addEventListener('message', this.receiveMessageResponse , false);
                } else if (window.attachEvent) {
                    window.attachEvent('onmessage', this.receiveMessageResponse);
                }

                this.socket = new easyXDM.Socket({
                    onMessage: function(message, origin){
                        try{
                            var msg = JSON.parse(message);
                            switch(msg.type){
                                case 'hash':
                                    window.location.hash = msg.data;
                                    break;
                            }
                        } catch(e){
                        }
                    }
                });

            },
            sendMessage: function(type, data){
                this.socket.postMessage(JSON.stringify({ type: type, data: data}));
            },
            receiveMessageResponse:function (response) {
                var self = this;
                if(response.data){
                    try {
                        var echoedUser = JSON.parse(response.data);
                        this.EvAg.trigger('login/complete', echoedUser);
                    } catch(e){

                    }
                }

            }
        });
    }
);
