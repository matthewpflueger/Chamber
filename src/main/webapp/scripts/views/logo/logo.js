define(
    [
        'jquery',
        'backbone',
        'underscore',
        'hgn!views/logo/logo'
    ],
    function($, Backbone, _, templateLogo){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element = $(options.el);
                this.modelPartner = options.modelPartner;
                this.properties = options.properties;
                this.modelPartner.on("change", this.render);
                this.render();
            },
            render: function(){
                console.log(this.modelPartner.toJSON());
                var template = templateLogo({ properties: this.properties, partner: this.modelPartner.toJSON() });
                this.element.html(template)
            }
        })
    }
)