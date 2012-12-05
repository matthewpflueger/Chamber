define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'receiveMessageResponse');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                if(window.addEventListener){
                    window.addEventListener('message', this.receiveMessageResponse , false);
                } else if (window.attachEvent) {
                    window.attachEvent('onmessage', this.receiveMessageResponse);
                }
            },
            receiveMessageResponse:function (response) {
                var self = this;
                if(response.data === "echoed-open"){
                    $('body').show();
                    self.EvAg.trigger('exhibit/init', { endPoint : "/partner/" + this.properties.partnerId });
                    _gaq.push(['_trackEvent', 'Widget', 'Open', this.properties.partnerId]);
                    self.EvAg.trigger('isotope/relayout');
                } else if(response.data){
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
