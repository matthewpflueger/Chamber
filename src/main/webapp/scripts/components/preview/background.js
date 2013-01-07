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
                this.properties = options.properties;
                this.element = $(this.el);
                this.options.EvAg.bind('background/show', this.show);
            },
            show: function(){
                this.element.show();
            }
        });
    }
);
