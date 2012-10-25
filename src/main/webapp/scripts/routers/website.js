define(
    ['jquery', 'backbone', 'underscore'],
    function($, Backbone, _){
        return Backbone.Router.extend({
            initialize: function(options) {
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
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
                "story/:id": "story",
                "write/:type/:id" : "writeStory",
                "write/" : "writeStory",
                "write": "writeStory",
                "communities" : "cList",
                //
                "!": "explore",
                "!me/friends": "friends",
                "!me/": "me",
                "!me": "me",
                "!user/:id": "user",
                "!communities": "cList",
                "!category/:category": "category",
                "!partner/:name/": "partnerFeed",
                "!partner/:name": "partnerFeed",
                "!story/:id": "story",
                "!write/:type/:id" : "writeStory",
                "!write/" : "writeStory",
                "!write": "writeStory"
            },
            fix: function(){
                window.location.href = "#";
            },
            loadPage: function(page, options){
                this.EvAg.trigger('exhibit/init', options);
                this.EvAg.trigger('page/change', page);
                this.EvAg.trigger("story/hide");
                this.EvAg.trigger('fade/hide');
                _gaq.push(['_trackPageview', this.page]);
            },
            cList: function(){
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.loadPage("communities", { endPoint: "/tags", title: "Communities"});
                }
            },
            explore: function(){
                if(this.page != window.location.hash){
                    this.page = "#!";
                    this.loadPage("explore", { endPoint: "/me/feed", title: "" });
                }
            },
            partnerFeed: function(partnerId) {
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.loadPage("partner", { endPoint: "/partner/" + partnerId });
                }
            },
            me: function() {
                if(this.page != window.location.hash){
                    this.page = "#!me";
                    this.loadPage("exhibit", { endPoint: "/me/exhibit", personal: true, title: "My Stories"});
                }
            },
            friends: function() {
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.loadPage("friends",  { endPoint: "/me/friends", title: "My Friends"});
                }
            },
            user: function(id){
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    if(this.properties.echoedUser.id === id || this.properties.echoedUser.screenName === id) this.loadPage("exhibit", { endPoint: "/me/exhibit", personal: true, title: "My Stories"});
                    else this.loadPage('user', { endPoint: "/user/" + id });
                }
            },
            community: function(communityId){
                if(this.page != window.location.hash){
                    this.page = window.location.hash;
                    this.loadPage("category", { endPoint: "/category/" + communityId, title: communityId })
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
                            if(this.properties.echoedUser !== undefined){
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
