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
                this.EvAg = options.EvAg;
                this.modelPartner = options.modelPartner;

                if(this.modelPartner){
                    this.modelPartner.on("change", this.pageChange);
                }

                this.EvAg.bind('msg/send', this.sendMessage);
                this.properties = options.properties;
                if(window.addEventListener){
                    window.addEventListener('message', this.receiveMessageResponse , false);
                } else if (window.attachEvent) {
                    window.attachEvent('onmessage', this.receiveMessageResponse);
                }
                try {
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
                } catch(e){

                }
                window.onhashchange = function(){
                    self.sendMessage("hashChange", window.location.hash);
                }
            },
            pageChange: function(){
                this.sendMessage("contextChange", this.modelPartner.getPartnerPath());
            },
            sendMessage: function(type, data){
                try{
                    this.socket.postMessage(JSON.stringify({ type: type, data: data}));
                } catch(e){

                }

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
