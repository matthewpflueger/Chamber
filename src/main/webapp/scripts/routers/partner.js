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
                "" : "home",
                "install" : "install",
                "moderate" : "moderate"
            },
            install: function(){
                this.EvAg.trigger('install/show');
                this.EvAg.trigger('moderate/hide');
            },
            home: function(){
                this.EvAg.trigger('moderate/show');
                this.EvAg.trigger('install/hide');
            }
        })
    }
)
