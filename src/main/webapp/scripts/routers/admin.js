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
                "": "home",
                "partners": "partners"
            },
            home: function(){
                this.EvAg.trigger('moderate/show');
            },
            partners: function(){
                this.EvAg.trigger('partnerList/show');
            }
        })
    }
);
