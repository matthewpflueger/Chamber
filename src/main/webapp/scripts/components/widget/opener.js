define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
            },
            events: {
                "click .echoed-opener": "show",
                "click #echoed-opener": "show"
            },
            show: function(){
                this.EvAg.trigger('overlay/show');
            }
        })
    }
);
