define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                this.EvAg = options.EvAg;
                this.element = $(options.el);
            },
            events: {
                "click": "close"
            },
            close: function(){
                window.setTimeout(function(){
                    window.top.postMessage("echoed-close","*");
                }, 0);
            }
        });
    }
)
