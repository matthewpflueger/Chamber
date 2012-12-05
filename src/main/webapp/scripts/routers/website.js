define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils'
    ],
    function($, Backbone, _, utils){
        return Backbone.Router.extend({
            initialize: function(options) {
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.modelUser = options.modelUser;
                this.EvAg.bind("hash/reset", this.resetHash);
                this.EvAg.bind("router/me", this.me);
                this.page = null;
            },
            routes:{
                "_=_" : "fix",
                "": "explore",
                "me/friends": "friends",
                "me/": "me",
                "me": "me",
                "user/:id": "user",
                "community/:community": "community",
                "partner/:name/": "partnerFeed",
                "partner/:name": "partnerFeed",
                "topic/:id": "topic",
                "story/:id": "story",
                "write/:type/:id" : "writeStory",
                "write/" : "writeStory",
                "write": "writeStory",
                "communities" : "cList",
                "topic/:topic": "topic",
                "!": "explore",
                "!me/friends": "friends",
                "!me/": "me",
                "!me": "me",
                "!user/:id": "user",
                "!communities": "cList",
                "!partner/:name/": "partnerFeed",
                "!partner/:name": "partnerFeed",
                "!story/:id": "story",
                "!write/:type/:id" : "writeStory",
                "!write/" : "writeStory",
                "!write": "writeStory",
                "!topic/:id": "topic"
            },
            fix: function(){
                window.location.href = "#";
            },
            requestFeed: function(endPoint, callback){
                var jsonUrl = this.properties.urls.api + '/api/' + endPoint;
                utils.AjaxFactory({
                        url: jsonUrl,
                        dataType: 'json',
                        success: function(data){
                            callback(jsonUrl, data);
                        }
                })();
            },
            loadPage: function(page, options){
                this.EvAg.trigger('fade/hide');
                this.EvAg.trigger('exhibit/init', options);
                this.EvAg.trigger('page/change', page);
                this.EvAg.trigger("story/hide");

                _gaq.push(['_trackPageview', this.page]);
            },
            cList: function(){
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.requestFeed("/tags", function(jsonUrl, data){
                        self.loadPage("communities", { jsonUrl: jsonUrl, data: data });
                        self.EvAg.trigger('title/update', { title: "Communities", image: data.headerImage });
                    });
                }
            },
            explore: function(){
                var self = this;
                if(this.page != window.location.hash){
                    this.page = "#!";
                    this.requestFeed("/me/feed", function(jsonUrl, data){
                        self.loadPage("explore", { jsonUrl: jsonUrl, data: data });
                        self.EvAg.trigger('title/update', { type:"echoed", title: "Share Your Stories With the World", image: data.headerImage });
                    });
                }
            },
            partnerFeed: function(partnerId) {
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.requestFeed("/partner/" + partnerId, function(jsonUrl, data){
                        self.loadPage("partner", { jsonUrl: jsonUrl, data: data});
                        self.EvAg.trigger('title/update', { type: "partner", partnerId: partnerId, title: "Stories from " + data.partner.name, image: data.headerImage });
                    });
                }
            },
            topic: function(topicId){
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.requestFeed("/topic/" + topicId, function(jsonUrl, data){
                        self.loadPage("topic", { jsonUrl: jsonUrl, data: data});
                        self.EvAg.trigger('title/update', { type: "topic", title: "Topic: " + data.topic.title, image: data.headerImage })
                    })
                }
            },
            me: function() {
                var self = this;
                if(this.page != window.location.hash){
                    this.page = "#!me";
                    this.requestFeed("/me/exhibit", function(jsonUrl, data){
                        self.loadPage("user", { jsonUrl: jsonUrl, data: data, personal: true} );
                        self.EvAg.trigger('title/update', { title : "My Stories"});
                    });
                }
            },
            friends: function() {
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.requestFeed("/me/following", function(jsonUrl, data){
                        self.loadPage("friends", { jsonUrl: jsonUrl, data: data});
                        self.EvAg.trigger("title/update", { title: "My Friends"});
                    });
                }
            },
            user: function(id){
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    if(this.modelUser.is(id)){
                        this.requestFeed("/me/exhibit", function(jsonUrl, data){
                            self.loadPage("user", { jsonUrl: jsonUrl, data: data, personal : true});
                            self.EvAg.trigger('title/update', { title : "My Stories"});
                        });
                    } else {
                        this.requestFeed("/user/" + id, function(jsonUrl, data){
                            self.loadPage("user", { jsonUrl: jsonUrl, data: data});
                            self.EvAg.trigger('title/update', { title: data.echoedUser.name });
                        })
                    }
                }
            },
            community: function(communityId){
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.requestFeed("/category/" + communityId, function(jsonUrl, data){
                        self.loadPage("category", {jsonUrl: jsonUrl, data: data});
                        self.EvAg.trigger('title/update', { type: "community", communityId: communityId, title: "Stories in the " + communityId + " community", image: data.headerImage});
                    });
                }
            },
            writeStory: function(type, id){
                if(this.page === null){
                    switch(type){
                        case "partner":
                            this.partnerFeed(id);
                            this.page = "#!partner/" + id;
                            break;
                        default:
                            if(this.modelUser.isLoggedIn()){
                                this.me();
                                this.page = "#!me";
                            } else {
                                this.explore();
                                this.page = "#";
                            }
                            break;
                    }
                }
                this.oldPage = this.page;
                this.EvAg.trigger("field/show",id , type);
            },
            story: function(id){
                if(this.page === null) {
                    this.explore();
                    this.page = "#!";
                }
                this.oldPage = this.page;
                this.oldTitle = $('title').html();
                _gaq.push(['_trackPageview', window.location.hash]);

                this.EvAg.trigger("story/show", id);
                this.EvAg.trigger("page/change", "story");
            },
            resetHash: function(){

                if(this.oldPage){
                    $('title').html(this.oldTitle);
                    window.location.hash = this.oldPage;
                } else {
                    window.location.hash = "#!";
                }

            }
        });
    }
);
