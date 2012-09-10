define(
    ['jquery', 'backbone', 'underscore'],
    function($, Backbone, _){
        return Backbone.Router.extend({
            initialize: function(options) {
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.EvAg.bind('hash/reset', this.resetHash);
                this.page = null;
            },
            routes:{
                "_=_" : "fix",
                "": "explore",
                "!": "explore",
                "story/:id": "story",
                "write/:type/:id" : "writeStory",
                "write/" : "writePartner",
                "write": "writePartner",
                "!story/:id": "story"
            },
            explore: function(){

                if(this.page !== "#!"){
                    //this.EvAg.trigger('exhibit/init', { endPoint : "/partner/" + this.properties.partnerId });
                    //this.page = "#!";
                }
            },
            writePartner: function(){
                this.writeStory('partner', this.properties.partnerId );
            },
            writeStory: function(type, id){
                if(this.page === null){
                    this.explore();
                    this.page = "#!";
                }
                this.oldPage = this.page;
                this.EvAg.trigger("field/show", id, type);
            },
            story: function(id){
                if(this.page === null) {
                    this.explore();
                    this.page = "#!";
                }
                this.oldPage = this.page;
                this.EvAg.trigger("story/show", id);
            },
            resetHash: function(){
                window.location.hash = "#!";
            }
        });
    }
);
