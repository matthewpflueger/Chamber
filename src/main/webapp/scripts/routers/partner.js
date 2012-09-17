/**
 * Created with IntelliJ IDEA.
 * User: jonlwu
 * Date: 9/16/12
 * Time: 1:27 AM
 * To change this template use File | Settings | File Templates.
 */
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
                "install": "install"
            },
            install: function(){
                this.EvAg.trigger('install/show');
            },
            home: function(){
                this.EvAg.trigger('home/show');
            }
        })
    }
)
