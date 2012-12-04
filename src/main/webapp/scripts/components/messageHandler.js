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
            receiveMessageResponse: function(response) {
                var echoedUser = JSON.parse(response.data);
                this.EvAg.trigger('user/login', echoedUser);
            }
        });
    }
);
