define(
    ['jquery', 'backbone', 'underscore'],
    function($, Backbone, _){
        return Backbone.View.extend({
            el: "#header-nav",
            initialize: function(options){
                _.bindAll(this, 'click', 'login');
                this.element = $(this.el);
                this.EvAg = options.EvAg;
                this.EvAg.bind('user/login', this.login);
                this.li = this.element.find('li');
                this.ul = this.element.find('ul');
            },
            events:{
                "click li": "click"
            },
            login: function(){
                $('<li class="icon_friends" href="#!me/friends" id="friends_nav"></li>').text('My Friends').hide().appendTo(this.ul).fadeIn();
                $('<li class="icon_me" href="#!me" id="me_nav"></li>').text('My Stories').hide().appendTo(this.ul).fadeIn();

            },
            click: function(e){
                this.li.removeClass("current");
                $(e.target).addClass("current");
                window.location.hash = $(e.target).attr("href");
            }
        });
    }
);