define(
    'components/notifications',
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'collections/notifications',
        'hgn!templates/notifications/notificationsList',
        'hgn!templates/notifications/notification'
    ],
    function($, Backbone, _, utils, CollectionNotifications,  tmpNotificationsList, tmpNotification){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'init');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.element = $(options.el);
                this.modelUser = options.modelUser;
                this.modelUser.on("change:id", this.init);

                this.collectionNotifications = new CollectionNotifications({ properties: this.properties });
                var template = tmpNotificationsList();
                this.element.html(template);

                this.collectionNotifications.on("add", function(notification){
                    var template = tmpNotification(notification);
                    self.list.append(template);
                });

                this.list = $('#notifications-list');
                this.menu = $('#notifications-menu');
                this.header = $('#notifications-list-header');
                this.text = $('#notifications-text');
                this.checkbox = $('#receive-notification-email-cb');
                this.init();
            },
            render: function(){
                this.text.text(this.collectionNotifications.length);
                this.header.text('New Notifications (' + this.collectionNotifications.length + ")");
                if(this.collectionNotifications.length > 0) this.text.removeClass('off');
                else this.text.addClass("off");
                this.element.show();
            },
            init: function(){
                var self = this;
                if(this.modelUser.isLoggedIn()){
                    utils.AjaxFactory({
                        url: Echoed.urls.api + "/api/me/settings",
                        success: function(settings){
                            if(settings.receiveNotificationEmail === true){
                                self.checkbox.attr('checked',true)
                            }
                        }
                    })();

                    this.collectionNotifications.fetch({
                        success: function(collection, xhr, options){
                            self.render();
                        }
                    });
                }
            },
            events: {
                "click .notification": "redirect",
                "click #notifications": "toggle",
                "click #receive-notification-email-cb": "setEmailSetting"
            },
            setEmailSetting: function(){
                var self = this;
                var bNotify = self.checkbox.is(':checked');
                utils.AjaxFactory({
                    url: self.properties.urls.api + "/api/me/settings",
                    type: 'POST',
                    processData: false,
                    contentType: "application/json",
                    data: JSON.stringify({
                        receiveNotificationEmail: bNotify
                    }),
                    success: function(response){
                        if(response.receiveNotificationEmail === true){
                            self.checkbox.attr('checked',true)
                        }
                    }
                })();
            },
            redirect: function(ev){
                this.toggle();
                window.location.hash = $(ev.currentTarget).attr("href");
            },
            toggle: function(){
                this.element.toggleClass('on');
                if(this.on === true){
                    this.hide();
                    this.on = false;
                } else {
                    this.on = true;
                    this.show();
                }
            },
            show: function(){
                this.menu.show();
                this.collectionNotifications.markAsRead();
            },
            hide: function(){
                this.menu.hide();
            }
        });
    }
)
