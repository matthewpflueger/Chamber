define(
    [
        'jquery',
        'backbone',
        'underscore',
        'hgn!views/header/header',
        'components/user',
        'components/notifications',
        'components/widgetCloser',
        'views/logo/logo'

    ],
    function($, Backbone, _, templateHeader, User, Notifications, WidgetCloser, Logo){
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
                    modelPartner:   this.modelPartner,
                    EvAg:           this.EvAg
                });
                this.logo = new Logo({
                    el:             "#logo",
                    properties:     this.properties,
                    modelUser:      this.modelUser,
                    modelContext:   this.modelContext,
                    modelPartner:   this.modelPartner,
                    EvAg:           this.EvAg
                });
                this.element.addClass("white");
            }
        });
    }
)