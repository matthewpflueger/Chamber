define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.Router.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
            },
            routes: {
                "": "home"
            },
            home: function(){
                this.EvAg.trigger('moderate/show');
            }
        })
    }
);
