define(
    [
        'jquery',
        'backbone',
        'underscore',
        'models/context',
        'components/utils',
        'views/follow/follow',
        'hgn!templates/title/title',
        'hgn!templates/title/topic'
    ],
    function($, Backbone, _, ModelContext, utils, Follow, templateTitle, templateTopic){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element = $(options.el);
                this.titleEl = $('#title');
                this.properties =   options.properties;
                this.modelContext = options.modelContext;
                this.modelUser =    options.modelUser;
                this.modelContext.on("change", this.render);
                this.EvAg = options.EvAg;
            },
            render: function(){
                var view = {
                    context: this.modelContext.toJSON(),
                    baseUrl: this.modelContext.get("contextType").toLowerCase() + "/" + this.modelContext.id + "/"
                };
                this.element.html(templateTitle(view));
                this.follow = new Follow({ el: "#title-follow", properties: this.properties, modelUser: this.modelUser, followId: this.modelContext.id, type: this.modelContext.get("contextType") });
                this.titleText = $('#title-text');
                this.titleBody = $('#title-body');
                if(view.context.contextType === "partner") this.EvAg.trigger("page:change", view.context.partner.domain);
                this.element.show();
            }
        });
    }
);
