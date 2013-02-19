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
                this.modelPartner = options.modelPartner;
                this.modelPartner.on("change", this.toggle);
                this.element = $(this.el);
            },
            toggle: function(domain){
                var partner = this.modelPartner.toJSON();
                if(partner.name === 'Echoed'){
                    this.element.removeClass("black")
                } else {
                    this.element.addClass("black")
                }
            }
        });
    }
)