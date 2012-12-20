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
                this.EvAg.trigger("msg/send", "close", null)

            }
        });
    }
)
