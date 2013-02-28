define(
    ['jquery', 'backbone', 'underscore', 'hgn!templates/nav/nav'],
    function($, Backbone, _, templateNav){
        return Backbone.View.extend({
            el: "#nav-container",
            initialize: function(options){
                _.bindAll(this);
                this.element =      $(this.el);
                this.EvAg =         options.EvAg;
                this.modelUser =    options.modelUser;
                this.modelPartner = options.modelPartner;
                this.modelUser.on("change:id", this.render);
                this.modelPartner.on("change", this.changePartner);
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
            },
            changePartner: function(){
                var partner = this.modelPartner.toJSON();
                if(partner.name === "Echoed"){
                    this.element.addClass('white');
                    this.element.removeClass('black');
                } else {
//                    this.element.addClass('black');
//                    this.element.removeClass('white');
                }
            }
        });
    }
);