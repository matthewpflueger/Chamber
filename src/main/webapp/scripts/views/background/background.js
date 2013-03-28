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
                this.options.EvAg.bind('background/update', this.update);
            },
            update: function(url){
                if(url.indexOf("echoed.com") > -1){
                    this.element.hide();
                } else{
                    if(url.indexOf("http") === -1) url = "http://" + url;
                    this.element.attr("src", url);
                    this.element.show();
                }

            },
            show: function(){
                this.element.show();
            }
        });
    }
);
