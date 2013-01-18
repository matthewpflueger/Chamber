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
                this.properties =   options.properties;
                this.element =      $(this.el);

                this.options.EvAg.bind('background/show',   this.show);
                this.options.EvAg.bind('background/update', this.update);
            },
            update: function(url){
                this.element.attr("src", url);
            },
            show: function(){
                this.element.show();
            }
        });
    }
);
