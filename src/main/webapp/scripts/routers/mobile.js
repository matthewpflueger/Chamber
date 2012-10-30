define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.Router.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.exhibitLoaded = false;
            },
            routes: {
                "": "home",
                "story/:id" : "story",
                "!story/:id" : "story",
                "login": "login"
            },
            login: function(){
                this.EvAg.trigger('page/show', "login");
            },
            loadPage: function(page, options){
                this.EvAg.trigger('exhibit/init', options);
                this.EvAg.trigger('page/show', 'exhibit');
            },
            home: function(){
                this.loadPage("explore", { endPoint: "/me/feed", title: "" });
            },
            story: function(id){
                if(this.exhibitLoaded === false) this.EvAg.trigger('exhibit/init', { endPoint: "/me/feed", title: "" });
                if(this.lastPage ===  'story') {
                    this.EvAg.trigger('story/change', id);
                } else {
                    this.EvAg.trigger('story/show', id);
                    this.EvAg.trigger('page/show', 'story');
                }
                this.lastPage = 'story';
            }
        })
    }
)