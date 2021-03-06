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
                this.modelUser.on("change", this.change);
                this.EvAg.bind("hash:reset", this.resetHash);
                this.EvAg.bind("router/me", this.me);
                this.currentRequest = null;
                this.page = null;
            },
            routes:{
                "_=_":                                          "fix",
                "":                                             "feed",
                "story/:id":                                    "story",
                "photo/:id":                                    "photo",
                "write/:id" :                                   "write",
                "write":                                        "write",
                "me/feed/:type":                                "feed",
                "me/feed/:type/":                               "feed",
                "explore":                                      "explore",
                "explore/:type":                                "explore",
                "partner/:id(/:contentType)/page/*path":        "partnerPage",
                ":context/:id(/:contentType)":                  "content",
                ":context/:id":                                 "content",
                ":context/:id(/:type)(/:type2)":                "content"
            },
            fix: function(){
                window.location.href = "#";
            },
            change: function(){
                this.feed();
            },
            partnerPage: function(id, type, path){
                var self = this;
                var url = "partner/" + id;
                if(type) {
                    url += "/" + type;
                }
                var page = window.location.hash;
                if(this.page !== page) {
                    this.page = page;
                    this.requestFeed(url, function(jsonUrl, data){
                        self.loadPage("partner", { jsonUrl: jsonUrl, data: data, personal: true } );
                        if(Echoed.pageTitle) {
                            self.modelContext.setPage(path, Echoed.pageTitle);
                            Echoed.pageTitle = null;
                            Echoed.path =  null;
                        }
                    }, {
                        contentPath: path
                    });
                }
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
            requestFeed: function(endPoint, callback, postData){
                var self = this;
                var jsonUrl = this.properties.urls.api + '/api/' + endPoint;
                var timeStamp = new Date().getTime().toString();
                self.currentRequest = timeStamp;
                self.EvAg.trigger('infiniteScroll/lock');
                utils.AjaxFactory({
                    url: jsonUrl,
                    dataType: 'json',
                    data: postData,
                    success: function(data){
                        self.EvAg.trigger('infiniteScroll/unlock');
                        if(self.currentRequest === timeStamp) callback(jsonUrl, data, timeStamp);
                    }
                })();
            },
            loadPage: function(page, options){
                if(_.isEmpty(options.data.context)){
                    this.modelContext.clear();
                } else {
                    this.modelContext.clear();
                    console.log(options.data.context);
                    this.modelContext.set(options.data.context);
                }
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
