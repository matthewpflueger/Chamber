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
                this.EvAg.bind('user/login', this.login);
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
            login: function(echoedUser){
                this.properties.echoedUser = echoedUser;
                console.log(echoedUser);
                var image = $('<img id="u-i-i" height="30px" width="30px" />').attr('src', utils.getProfilePhotoUrl(echoedUser));
                var ui = $('<div id="user-image"></div>').append(image);
                $("#user-text").html(this.properties.echoedUser.name);
                $('#user-list').find('ul').append('<li class="user-list-item" href="logout">Logout</li>');
                this.element.prepend(ui);
                this.element.show();
            }
        });
    }
)
