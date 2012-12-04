define(
    'components/notifications',
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils'
    ],
    function($, Backbone, _, utils){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'init');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.element = $(options.el);
                this.modelUser = options.modelUser;
                this.modelUser.on("change:id", this.init);
                this.list = $('#notifications-list');
                this.menu = $('#notifications-menu');
                this.header = $('#notifications-list-header');
                this.text = $('#notifications-text');
                this.checkbox = $('#receive-notification-email-cb');
                this.init();
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
                    utils.AjaxFactory({
                        url: self.properties.urls.api + "/api/notifications",
                        success: function(notifications){
                            self.count = notifications.length;
                            self.list.empty();
                            self.text.html(self.count);
                            self.header.text('New Notifications (' + self.count + ")");
                            if(self.count > 0){
                                $.each(notifications, function(index, notification){
                                    var ntf = $('<div></div>')
                                            .addClass('notification')
                                            .append($('<span class="bold"></span>').text(notification.value.subject))
                                            .append($('<span></span>').text(" " + notification.value.action + " "))
                                            .append($('<span class="bold"></span>').text(notification.value.object))
                                            .attr("id", notification.id);

                                    if (notification.value.storyId !== undefined)
                                        ntf.attr("href", "#story/" + notification.value.storyId)
                                    else if (notification.value.followerId !== undefined)
                                        ntf.attr("href", "#user/" + notification.value.followerId)

                                    self.list.append(ntf)
                                });
                            } else {
                                self.text.addClass("off");
                            }
                            self.element.show();
                        }
                    })();
                }
            },
            markAsRead: function(){
                var self = this;
                var id;
                var ids = [];
                if(this.count > 0){
                    $.each(this.list.children(), function(index, node){
                        ids.push($(node).attr('id'));
                    });
                    utils.AjaxFactory({
                        url: self.properties.urls.api + "/api/notifications",
                        type: "POST",
                        data: {
                            'ids': ids
                        },
                        traditional: true
                    })();
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
                this.markAsRead();
            },
            hide: function(){
                this.menu.hide();
            }
        });
    }
)
