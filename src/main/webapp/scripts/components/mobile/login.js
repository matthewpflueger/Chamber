define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'text!templates/mobile/login.html'
    ],
    function($, Backbone, _, utils, templateLogin){
        return Backbone.View.extend({
            el: "#login",
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.EvAg.bind('login/show',this.render);
                this.EvAg.bind('user/login',this.login);
                this.EvAg.bind('page/show', this.pageLoad);
                this.el = options.el;
                this.element = $(options.el);
            },
            pageLoad: function(option){
                if(option === "login"){
                    this.render("exhibit");
                    this.element.fadeIn();

                } else {
                    this.element.fadeOut();
                }
            },
            login: function(){
                this.EvAg.trigger('page/show', this.returnPage);
                this.element.hide();
            },
            render: function(returnPage){
                var self = this;
                self.returnPage = returnPage;
                self.element.html(_.template(templateLogin));
                $("#fb-login").attr("href", utils.getFacebookLoginUrl("redirect/close")).attr("target","_blank");
                $("#tw-login").attr("href", utils.getTwitterLoginUrl("redirect/close")).attr("target","_blank");
                $('#user-login').attr('href', self.properties.urls.api + "/" +utils.getLoginRedirectUrl("redirect/close"));
                $('#user-signup').attr("href", self.properties.urls.api + "/" +utils.getSignUpRedirectUrl("redirect/close"));
                this.element.fadeIn();
            }
        })
    }
);
