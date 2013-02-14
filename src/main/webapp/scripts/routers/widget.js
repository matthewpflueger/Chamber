define(
    ['jquery', 'backbone', 'underscore', 'components/utils'],
    function($, Backbone, _, utils){
        return Backbone.Router.extend({
            initialize: function(options) {
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.modelContext = options.modelContext;
                this.EvAg.bind('hash/reset', this.resetHash);
                this.EvAg.bind('router/me', this.reload);
                this.page = null;
            },
            routes:{
                "_=_":                  "fix",
                "":                     "explore",
                "!":                    "explore",
                "home":                 "reload",
                "partner/:id/:type":    "partner",
                "partner/:id/:type/":   "partner",
                "topic/:id":            "topic",
                "story/:id":            "story",
                "photo/:id":            "photo",
                "!story/:id":           "story",
                "!photo/:id":           "photo",
                "write/:id" :           "write",
                "write/":               "write",
                "write":                "write"
            },
            partner: function(id, type){
                var self = this;
                var endPoint = "/partner/" + id;
                if(type) endPoint += "/" + type;
                this.requestFeed(endPoint, function(jsonUrl, data){
                    self.modelContext.set(data.context);
                    self.EvAg.trigger("exhibit/init", { jsonUrl: jsonUrl, data: data });
                })
            },
            reload: function(){
                var self = this;
                var url = "/partner/" + this.properties.partnerId;
                if(this.page !== url){
                    this.page = url;
                    this.requestFeed(url, function(jsonUrl, data){
                        self.modelContext.set(data.context);
                        self.EvAg.trigger("exhibit/init", { jsonUrl: jsonUrl, data: data });
                    });
                }
                _gaq.push(['_trackEvent', 'Widget', 'Open', this.properties.partnerId]);
            },
            explore: function(){
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
            write: function(id){
                if(this.page === null){
                    this.reload();
                }
                this.oldPage = this.page;
                if(id)  this.EvAg.trigger("input:edit", id);
                else    this.EvAg.trigger("input:write");

                _gaq.push(['_trackEvent', 'Widget', 'Write', this.properties.partnerId]);
            },
            photo: function(id){
                if(this.page === null) {
                    this.reload();
                }
                this.oldPage = this.page;
                this.EvAg.trigger("content:lookup", id);
            },
            story: function(id){
                if(this.page === null) {
                    this.reload();
                }
                this.oldPage = this.page;
                this.EvAg.trigger("content:lookup", id);
                _gaq.push(['_trackEvent', 'Widget', 'Story',    this.properties.partnerId]);
                _gaq.push(['_trackEvent', 'Story',  'Open',     this.properties.partnerId]);
            },
            resetHash: function(){
                window.location.hash = this.oldPage;
            }
        });
    }
);
