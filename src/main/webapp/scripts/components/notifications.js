define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'collections/notifications',
        'models/userSettings',
        'hgn!templates/notifications/notificationsList',
        'hgn!templates/notifications/notification'
    ],
    function($, Backbone, _, utils, CollectionNotifications, ModelUserSettings, tmpNotificationsList, tmpNotification){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'init');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.element = $(options.el);
                this.modelUser = options.modelUser;
                this.modelUser.on("change:id", this.init);

                this.collectionNotifications = new CollectionNotifications(null, { properties: this.properties });
                this.userSettings = new ModelUserSettings({}, { properties: this.properties });

                var template = tmpNotificationsList();
                this.element.html(template);

                this.list = $('#notifications-list');
                this.menu = $('#notifications-menu');
                this.header = $('#notifications-list-header');
                this.text = $('#notifications-text');
                this.checkbox = $('#receive-notification-email-cb');
                this.init();
            },
            render: function(){
                var self = this;
                this.text.text(this.collectionNotifications.length);
                this.header.text('New Notifications (' + this.collectionNotifications.length + ")");
                if(this.collectionNotifications.length > 0) this.text.removeClass('off');
                else this.text.addClass("off");

                this.collectionNotifications.each(function(notification){
                    var template = tmpNotification({ notification: notification.toJSON() });
                    self.list.append(template);
                });

                if(this.userSettings.get("receiveNotificationEmail") === true) this.checkbox.attr("checked", true);
                this.element.show();
            },
            init: function(){
                var self = this;
                if(this.modelUser.isLoggedIn()){
                    this.userSettings.fetch({
                        success: function(model, xhr, options){
                            self.render();
                        }
                    });
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
                this.userSettings.set("receiveNotificationEmail", self.checkbox.is(":checked"));
                this.userSettings.saveSettings();
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
);
