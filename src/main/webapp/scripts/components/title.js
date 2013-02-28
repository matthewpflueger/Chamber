define(
    [
        'jquery',
        'backbone',
        'underscore',
        'models/context',
        'components/utils',
        'views/follow/follow',
        'hgn!templates/title/title'
    ],
    function($, Backbone, _, ModelContext, utils, Follow, templateTitle){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element = $(options.el);
                this.properties =   options.properties;
                this.modelContext = options.modelContext;
                this.modelUser =    options.modelUser;
                this.modelPartner = options.modelPartner;
                this.modelContext.on("change", this.update);
                this.EvAg = options.EvAg;
            },
            update: function(){
                if(_.isEmpty(this.modelContext.toJSON())){
                    this.hide();
                } else {
                    this.render();
                }
            },
            render: function(){

                var content = this.modelContext.get("content");
                var contentType = this.modelContext.get("contentType");
                if(contentType !== null){
                    $.each(content, function(index, c) {
                        if(c.name === contentType.plural) content[index].isActive = true;
                    });
                }

                var view = {
                    context: this.modelContext.toJSON(),
                    baseUrl: this.modelContext.baseUrl(),
                    c: content
                };
                this.element.html(templateTitle(view));
                this.follow = new Follow({
                    el: "#title-follow",
                    EvAg: this.EvAg,
                    properties: this.properties,
                    modelUser: this.modelUser,
                    followId: this.modelContext.id,
                    type: this.modelContext.get("contextType")
                });
                if(view.context.contextType === "partner") {
                    this.modelPartner.set(view.context.partner);
                } else {
                    this.modelPartner.set({
                        name: "Echoed",
                        domain: "www.echoed.com"
                    });
                }
                this.element.show();
            },
            hide: function(){
                this.element.hide();
            }
        });
    }
);
