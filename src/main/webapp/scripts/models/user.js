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
                this.getFollowing();
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
            follow: function(followId, callback){
                var self = this;
                if(this.id !== followId){
                    var request = {
                        url: this.properties.urls.api + "/api/me/following/" + followId,
                        type: "PUT",
                        success: function(response){
                            this.following = response;
                            callback(self, response)
                        }
                    };
                    if(this.isFollowing(followId)) request.type = "DELETE";
                    utils.AjaxFactory(request)();
                }
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
            isFollowing: function(followId){
                var isFollowing = false;
                $.each(this.following, function(index, following){
                    if(following.echoedUserId === followId) isFollowing = true;
                });
                return isFollowing;
            }
        });
    }
);