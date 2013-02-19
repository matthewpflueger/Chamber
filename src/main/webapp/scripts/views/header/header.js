define(
    [
        'jquery',
        'backbone',
        'underscore',
        'hgn!views/header/header',
        'components/user',
        'components/notifications',
        'components/widgetCloser'

    ],
    function($, Backbone, _, templateHeader, User, Notifications, WidgetCloser){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.el =           options.el;
                this.element =      $(this.el);
                this.properties =   options.properties;
                this.EvAg =         options.EvAg;
                this.modelUser =    options.modelUser;
                this.modelContext = options.modelContext;
                this.modelPartner = options.modelPartner;
                this.modelPartner.on("change", this.change);
                this.render();
            },
            render: function(){
                var view = {
                    properties: this.properties
                };
                var template = templateHeader(view);
                this.element.html(template);
                this.user = new User({
                    el:             "#user",
                    properties:     this.properties,
                    modelUser:      this.modelUser,
                    modelContext:   this.modelContext,
                    EvAg:           this.EvAg
                });
                this.notifications = new Notifications({
                    el:             "#notifications-container",
                    properties:     this.properties,
                    modelUser:      this.modelUser,
                    modelContext:   this.modelContext,
                    EvAg:           this.EvAg
                });
                this.closer = new WidgetCloser({
                    el:             "#close",
                    properties:     this.properties,
                    modelUser:      this.modelUser,
                    modelContext:   this.modelContext,
                    EvAg:           this.EvAg
                });
                this.element.addClass("white");
            },
            change: function(){
                var partner = this.modelPartner.toJSON();
                if(partner.name === "Echoed"){
                    this.element.removeClass("black");
                    this.element.addClass("white");
                } else{
                    this.element.addClass("black");
                    this.element.removeClass("white");
                }
            }
        });
    }
)