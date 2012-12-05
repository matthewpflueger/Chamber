define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'hgn!templates/user/user'
    ],
    function($, Backbone, _, utils, templateUser){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.modelUser = options.modelUser;
                this.modelUser.on("change:id", this.render);
                this.el = options.el;
                this.element = $(this.el);
                this.render();
            },
            render: function(){
                var echoedUser = this.modelUser.toJSON();
                echoedUser.imageUrl = utils.getProfilePhotoUrl(echoedUser);
                this.element.html(templateUser(echoedUser));
                this.list = $('#user-list');
                console.log(this.list);
            },
            events: {
                "click li": "click",
                "click .user-login": "showLogin",
                "mouseenter": "show",
                "mouseleave": "hide"
            },
            showLogin: function(){
                this.EvAg.trigger('login/init');
            },
            show: function(){
                this.list.show();
            },
            hide: function(){
                this.list.hide();
            },
            click: function(ev){
                window.location = $(ev.currentTarget).attr('href');
            }
        });
    }
)
