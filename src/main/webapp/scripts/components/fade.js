define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this,'show','hide');
                this.EvAg = options.EvAg;
                this.el = options.el;
                this.properties = options.properties;
                this.element = $(this.el);
                this.EvAg.bind("fade/show", this.show);
                this.EvAg.bind("fade/hide", this.hide);
            },
            show: function(){
                $("body").addClass("noScroll");
                this.element.fadeIn();
            },
            hide: function(){
                this.element.fadeOut();
                $("body").removeClass("noScroll");
            }
        });
    }
)
