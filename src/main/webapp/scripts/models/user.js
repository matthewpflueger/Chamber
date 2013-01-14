define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils'
    ],
    function($, Backbone, _, utils){
        return Backbone.Model.extend({
            initialize: function(attr, options){
                this.properties = options.properties;
                this.following = [];
                this.followingPartners = [];
                this.getFollowing();
                this.getFollowingPartners();
            },
            is: function(id){
                return this.id === id || this.get('screenName') === id
            },
            isLoggedIn: function(){
                return this.has('id');
            },
            login: function(echoedUser){
                this.set(echoedUser)
            },
            follow: function(followId, type, callback){
                var self = this;
                var url = this.properties.urls.api + "/api/me/following/" + followId;
                if(type === "Partner") url = this.properties.urls.api + "/api/me/following/partners/" + followId;
                console.log(url);
                if(this.id !== followId){
                    var request = {
                        url: url,
                        type: "PUT",
                        success: function(response){
                            if(type === "Partner") self.followingPartners = response;
                            else self.following = response;
                            callback(self, response)
                        }
                    };
                    if(this.isFollowing(followId, "Partner")) request.type = "DELETE";
                    utils.AjaxFactory(request)();
                }
            },
            getFollowingPartners: function(){
                var url = this.properties.urls.api + "/api/me/following/partners";
                var self = this;
                utils.AjaxFactory({
                    url: url,
                    success: function(response){
                        self.followingPartners = response;
                    }
                })();
            },
            getFollowing: function(){
                var url = this.properties.urls.api + "/api/me/following";
                var self  = this;
                utils.AjaxFactory({
                    url: url,
                    success: function(response){
                        self.following = response;
                    }
                })();
            },
            isFollowing: function(followId, type){
                var isFollowing = false;
                if(type === "Partner"){
                    $.each(this.followingPartners, function(index, followingPartner){
                        if(followingPartner.partnerId === followId) isFollowing = true;
                    });
                } else {
                    $.each(this.following, function(index, following){
                        if(following.echoedUserId === followId) isFollowing = true;
                    });
                }
                return isFollowing;
            }
        });
    }
);