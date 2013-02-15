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
                this.EvAg.bind("hash:reset", this.resetHash);
                this.EvAg.bind("router/me", this.me);
                this.currentRequest = null;
                this.page = null;
            },
            routes:{
                "_=_":                      "fix",
                "!":                        "feed",
                "":                         "feed",
                "!story/:id":               "story",
                "story/:id":                "story",
                "!photo/:id":               "photo",
                "photo/:id":                "photo",
                "!write/:id" :              "write",
                "!write":                   "write",
                "write/:id" :               "write",
                "write":                    "write",
                "me/feed/:type":            "feed",
                "me/feed/:type/":           "feed",
                ":context/:id":             "content",
                ":context/:id/":            "content",
                ":context/:id/:type":       "content",
                ":context/:id/:type/":      "content",
                ":context/:id/:type/:type2":"content"
            },
            fix: function(){
                window.location.href = "#";
            },
            content: function(context, id, type, type2){
                var self =      this;
                var url =       context + "/" + id;
                if(context === "user" && this.modelUser.is(id)) url = "/me";
                if(type) url += "/" + type;
                if(type2) url += "/" + type2;
                var page = url;
                if(this.page !== page){
                    this.page = page;
                    this.requestFeed(url, function(jsonUrl, data){
                        self.loadPage(context, { jsonUrl: jsonUrl, data: data, personal: true} );
                    });
                }
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
                try{
                    _gaq.push(['_trackPageview', this.page]);
                } catch(e) {

                }
            },
            feed: function(type){
                var self = this;
                if(typeof(type) === "object") type = undefined;
                if(!this.modelUser.isLoggedIn()){
                    this.explore(type);
                } else {
                    var url = "me/feed";
                    if(type) url += "/" + type;
                    if(this.page != url){
                        this.page = url;
                        this.requestFeed(url, function(jsonUrl, data){
                            self.loadPage("explore", { jsonUrl: jsonUrl, data: data, personalized: true });
                        });
                    }
                }
            },
            explore: function(type){
                var self = this;
                var url = "public/feed"
                if(type) url += "/" + type;
                if(this.page != url){
                    this.page = url
                    this.requestFeed(url, function(jsonUrl, data){
                        self.loadPage("explore", { jsonUrl: jsonUrl, data: data });
                    });
                }
            },
            write: function(id){
                this.oldPage = this.page;

                if(id)  this.EvAg.trigger("input:edit", id);
                else    this.EvAg.trigger("input:write");
            },
            photo: function(id){
                if(this.page === null) {
                    this.explore();
                }
                this.oldPage = this.page;
                this.EvAg.trigger("content:lookup", id);
            },
            story: function(id){
                if(this.page === null) {
                    this.explore();
                }
                this.oldPage = this.page;
                this.oldTitle = $('title').html();
                this.EvAg.trigger("content:lookup", id);
                this.EvAg.trigger("page/change", "story");
                try {
                    _gaq.push(['_trackPageview', window.location.hash]);
                } catch(e) {

                }
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
