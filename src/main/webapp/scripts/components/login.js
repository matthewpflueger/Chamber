define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'hgn!templates/login/login'

    ],
    function($, Backbone, _, utils, templateLogin){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.modelUser = options.modelUser;
                this.EvAg.bind('login/complete', this.login);
                this.EvAg.bind('login/init', this.init);
                this.element = $(this.el);
            },
            login: function(echoedUser){
                this.modelUser.login(echoedUser);
                if(this.options.callback) {
                    this.EvAg.trigger(this.options.callback);
                    this.reset();
                }
                this.element.fadeOut();
            },
            reset: function(){
                this.options.loginText = null;
                this.options.callback = null;
                this.options.failCallback = null;
            },
            init: function(callback, text, failCallback){
                this.options.callback = callback;
                this.options.failCallback = failCallback;
                this.options.loginText = text;
                this.render();
            },
            render: function(){
                var view = {
                    fbUrl : utils.getFacebookLoginUrl("redirect/close"),
                    twUrl : utils.getTwitterLoginUrl("redirect/close"),
                    loginUrl : this.properties.urls.api + "/" + utils.getLoginRedirectUrl("redirect/close"),
                    signUpUrl :  this.properties.urls.api + "/" + utils.getSignUpRedirectUrl("redirect/close"),
                    imgUrl : this.properties.urls.images + "/logo_large.png",
                    loginText: this.options.loginText
                };
                var template = templateLogin(view);
                this.element.html(template);
                this.element.fadeIn();
            },
            events: {
                "click .login-button": "loginClick",
                "click .login-email": "loginClick",
                "click .fade": "close"
            },
            close: function(){

                this.element.fadeOut();
                this.element.empty();
            },
            loginClick: function(ev){
                var target = $(ev.currentTarget);
                var href = target.attr('href');
                window.open(href, "Echoed",'width=800,height=440,toolbar=0,menubar=0,location=0,status=1,scrollbars=0,resizable=0,left=0,top=0');
                return false;
            }
        });
    }
)
