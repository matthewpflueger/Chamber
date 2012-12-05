define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils'
    ],
    function($, Backbone, _, utils){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'login');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.modelUser = options.modelUser;
                this.modelUser.on("change:id", this.login);
                this.list = $('#user-list');
                this.el = options.el;
                this.element = $(this.el);
            },
            events: {
                "click li": "click",
                "mouseenter": "show",
                "mouseleave": "hide"
            },
            show: function(){
                this.list.show();
            },
            hide: function(){
                this.list.hide();
            },
            click: function(ev){
                window.location = $(ev.currentTarget).attr('href');
            },
            login: function(){
                var echoedUser = this.modelUser.toJSON();
                var image = $('<img id="u-i-i" height="30px" width="30px" />').attr('src', utils.getProfilePhotoUrl(echoedUser, this.properties.urls));
                var ui = $('<div id="user-image"></div>').append(image);
                $("#user-text").text(echoedUser.name);
                $('#user-list').find('ul').append('<li class="user-list-item" href="logout">Logout</li>');
                this.element.prepend(ui);
                this.element.show();
            }
        });
    }
)
