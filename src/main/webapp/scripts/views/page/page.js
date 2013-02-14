define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.View.extend({
            el: 'body',
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.modelUser = options.modelUser;
                this.modelContext = options.modelContext;
                this.EvAg.bind("page:change", this.toggle);
                this.element = $(this.el);
            },
            toggle: function(domain){
                if(domain === "echoed.com"){
                    this.element.removeClass("fade")
                } else {
                    this.element.addClass("fade")
                }
            }
        });
    }
)