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
                var partner = this.modelPartner.toJSON();
                var isEchoed = partner.name === "Echoed" ? true : false;
                var template = templateLogo({ properties: this.properties, partner: partner, isEchoed: isEchoed });
                this.element.html(template)
            }
        })
    }
)