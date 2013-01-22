define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'collections/contents'
    ],
    function($, Backbone, _, utils){
        return Backbone.Router.extend({
            initialize: function(options) {
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties =   options.properties;
                this.modelUser =    options.modelUser;
                this.modelContext = options.modelContext;
                this.colContent =   options.colContent;
                this.EvAg.bind("hash/reset", this.resetHash);
                this.EvAg.bind("router/me", this.me);
                this.currentRequest = null;
                this.page = null;
            },
            routes:{
                "_=_" : "fix",
                "": "explore",
                "me/friends": "friends",
                "me/": "me",
                "me": "me",
                "user/:id": "user",
                "user/:id/": "user",
                "user/:id/:type": "user",
                "user/:id/:type/": "user",
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
                var self = this;
                var jsonUrl = this.properties.urls.api + '/api/' + endPoint;
                var timeStamp = new Date().getTime().toString();
                self.currentRequest = timeStamp;
                utils.AjaxFactory({
                        url: jsonUrl,
                        dataType: 'json',
                        success: function(data){
                            if(self.currentRequest === timeStamp) callback(jsonUrl, data, timeStamp);
                        }
                })();
            },
            loadPage: function(page, options){
                this.modelContext.set(options.data.context);
                this.EvAg.trigger('exhibit/init', options);
                this.EvAg.trigger('page/change', page);
                _gaq.push(['_trackPageview', this.page]);
            },
            cList: function(){
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.requestFeed("/tags", function(jsonUrl, data){
                        self.loadPage("communities", { jsonUrl: jsonUrl, data: data });
                    });
                }
            },
            explore: function(){
                var self = this;
                if(this.page != window.location.hash){
                    this.page = "#!";
                    var url = "/me/feed";
                    if(!this.modelUser.isLoggedIn()) url = "/public/feed";
                    this.requestFeed(url, function(jsonUrl, data){
                        self.loadPage("explore", { jsonUrl: jsonUrl, data: data });
                    });
                }
            },
            partnerFeed: function(partnerId) {
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.requestFeed("/partner/" + partnerId, function(jsonUrl, data){
                        self.loadPage("partner", { jsonUrl: jsonUrl, data: data});
                    });
                }
            },
            topic: function(topicId){
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.requestFeed("/topic/" + topicId, function(jsonUrl, data){
                        self.loadPage("topic", { jsonUrl: jsonUrl, data: data});
                    })
                }
            },
            me: function() {
                var self = this;
                if(this.page != window.location.hash){
                    this.page = "#!me";
                    this.requestFeed("/me/", function(jsonUrl, data){
                        self.loadPage("user", { jsonUrl: jsonUrl, data: data, personal: true} );
                    });
                }
            },
            friends: function() {
                var self = this;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.requestFeed("/me/following", function(jsonUrl, data){
                        self.loadPage("friends", { jsonUrl: jsonUrl, data: data});
                    });
                }
            },
            user: function(id, type){
                var self =      this;
                var endPoint;
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    if(this.modelUser.is(id)){
                        endPoint = "/me/";
                        if(type) endPoint += "/" + type;
                        this.requestFeed(endPoint, function(jsonUrl, data){
                            self.loadPage("user", { jsonUrl: jsonUrl, data: data, personal : true});
                        });
                    } else {
                        endPoint = "/user/" + id;
                        if(type) endPoint += "/" + type;
                        this.requestFeed(endPoint, function(jsonUrl, data){
                            self.loadPage("user", { jsonUrl: jsonUrl, data: data});
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

                this.EvAg.trigger("content:show", id);
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
