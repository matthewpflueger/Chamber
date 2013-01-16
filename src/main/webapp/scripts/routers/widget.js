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
                "_=_" : "fix",
                "": "explore",
                "!": "explore",
                "home": "reload",
                "partner/:id/:type" : "partner",
                "partner/:id/:type/" : "partner",
                "topic/:id": "topic",
                "story/:id": "story",
                "write/:type/:id" : "writeStory",
                "write/" : "writePartner",
                "write": "writePartner",
                "!story/:id": "story",
                "!topic/:id": "topic"
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
                this.requestFeed("/partner/" + this.properties.partnerId, function(jsonUrl, data){
                    self.modelContext.set(data.context);
                    self.EvAg.trigger("exhibit/init", { jsonUrl: jsonUrl, data: data });
                    self.page = "home";
                });
                _gaq.push(['_trackEvent', 'Widget', 'Open', this.properties.partnerId]);
            },
            topic: function(topicId){
                var self = this;
                this.requestFeed("/topic/" + topicId, function(jsonUrl, data){
                    self.modelContext.set(data.context);
                    self.EvAg.trigger("exhibit/init", { jsonUrl: jsonUrl, data: data });
                });
            },
            explore: function(){
            },
            writePartner: function(){
                this.writeStory('partner', this.properties.partnerId );
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
            writeStory: function(type, id){
                if(this.page === null){
                    this.reload();
                    this.page = "#!";
                }
                this.oldPage = this.page;
                this.EvAg.trigger("field/show", id, type);
                _gaq.push(['_trackEvent', 'Widget', 'Write', this.properties.partnerId]);
            },
            story: function(id){
                if(this.page === null) {
                    this.reload();
                    this.page = "#!";
                }
                this.oldPage = this.page;
                this.EvAg.trigger("story/show", id);
                _gaq.push(['_trackEvent', 'Widget', 'Story', this.properties.partnerId]);
                _gaq.push(['_trackEvent', 'Story', 'Open', this.properties.partnerId]);
            },
            resetHash: function(){
                window.location.hash = "#!";
            }
        });
    }
);
