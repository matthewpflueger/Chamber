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
                this.EvAg = options.EvAg;
                this.el = options.el;
                this.properties = options.properties;
                this.element = $(this.el);
                this.EvAg.bind("fade/show", this.show);
                this.EvAg.bind("fade/hide", this.hide);
            },
            events: {
                "click": "click"
            },
            click: function(ev){
                if($(ev.target).attr("id") === "fade") this.EvAg.trigger('fade/click');
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
