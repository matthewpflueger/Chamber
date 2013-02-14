define(
    [
        'backbone',
        'jquery',
        'underscore',
        'hgn!views/follow/follow'
    ],
    function(Backbone, $, _, templateFollow){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element =      $(options.el);
                this.properties =   options.properties;
                this.modelUser =    options.modelUser;
                this.EvAg =         options.EvAg;
                this.modelUser.on("change", this.render);
                this.followId =     options.followId;
                this.type =         options.type;
                this.render();
            },
            events: {
                "click .button-follow" : "followClick"
            },
            followClick: function(){
                if(this.modelUser.isLoggedIn()){
                    this.follow();
                } else {
                    this.EvAg.trigger("login/init", this.follow, null);
                }
            },
            follow: function(){
                var self = this;
                this.modelUser.follow(this.followId, this.type, function(model, response){
                        self.render();
                    });
            },
            render: function(){
                if (this.followId === "feed" || this.modelUser.id === this.followId) return;

                var template = templateFollow({
                    isFollowing: this.modelUser.isFollowing(this.followId, this.type)
                });
                this.element.html(template);
            }
        })
    }
);