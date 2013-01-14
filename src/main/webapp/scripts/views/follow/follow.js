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
                this.element = $(options.el);
                this.properties = options.properties;
                this.modelUser = options.modelUser;
                this.followId = options.followId;
                this.type = options.type;
                this.render();
            },
            events: {
                "click .button-follow" : "follow"
            },
            follow: function(){
                var self = this;
                this.modelUser.follow(
                    this.followId,
                    this.type,
                    function(model, response){
                        self.render();
                    }
                );
            },
            render: function(){
                var view = {
                };
                if(this.modelUser.isLoggedIn() && !this.modelUser.is(this.followId)) {
                    view.isFollowing = this.modelUser.isFollowing(this.followId, this.type);
                    view.isLoggedIn = true;
                }
                var template = templateFollow(view);
                this.element.html(template);
            }
        })
    }
);