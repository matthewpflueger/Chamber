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
                "partners": "partners",
                "echoedusers": "echoedUsers"

            },
            home: function(){
                this.EvAg.trigger('page/change','moderate');
            },
            partners: function(){
                this.EvAg.trigger('page/change','partnerList');
            },
            echoedUsers: function(){
                this.EvAg.trigger('page/change','echoedUsers');
            }
        })
    }
);
