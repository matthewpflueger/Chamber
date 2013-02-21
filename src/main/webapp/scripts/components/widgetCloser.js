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
                this.modelPartner = options.modelPartner;
                this.modelPartner.on("change", this.render);
                this.element = $(options.el);
                this.render();
            },
            render: function(){
                if(this.modelPartner.isEchoed()){
                    this.element.hide();
                } else {
                    this.element.show();
                }
            },
            events: {
                "click": "close"
            },
            close: function(){
                this.EvAg.trigger("msg/send", "close", null)
            }
        });
    }
)
