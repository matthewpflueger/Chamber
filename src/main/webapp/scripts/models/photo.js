define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.Model.extend({
            initialize: function(attr, options){
                this.properties = options.properties;
            }
        });
    }
);