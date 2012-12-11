define(
    ['jquery', 'backbone', 'underscore', 'hgn!templates/nav/nav'],
    function($, Backbone, _, templateNav){
        return Backbone.View.extend({
            el: "#header-nav",
            initialize: function(options){
                _.bindAll(this);
                this.element = $(this.el);
                this.EvAg = options.EvAg;
                this.modelUser = options.modelUser;
                this.modelUser.on("change:id", this.render);
                this.render();
            },
            events:{
                "click li": "click"
            },
            render: function(){
                var view = this.modelUser.toJSON();
                var template = templateNav(view);
                this.element.html(template);
            },
            click: function(e){
                this.element.find("li").removeClass("current");
                $(e.target).addClass("current");
                window.location.hash = $(e.target).attr("href");
            }
        });
    }
);