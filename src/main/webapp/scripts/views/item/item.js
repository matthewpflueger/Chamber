define(
    [
        'jquery',
        'backbone',
        'underscore',
        'hgn!views/item/item',
        'models/story',
        'views/photo/photo',
        'components/story'
    ],
    function($, Backbone, _, tmpItem, ModelStory, PhotoView, StoryView ){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);

                this.modelUser =    options.modelUser;
                this.modelPartner = options.modelPartner;
                this.EvAg =         options.EvAg;
                this.element =      $(options.el);
                this.properties =   options.properties;

                this.element.html(tmpItem());
                this.body =         $('#item-body');
                this.photoView =    new PhotoView({ el: this.body, modelUser: this.modelUser, modelPartner: this.modelPartner, EvAg: this.EvAg, properties: this.properties });
                this.storyView =    new StoryView({ el: this.body, modelUser: this.modelUser, modelPartner: this.modelPartner, EvAg: this.EvAg, properties: this.properties });

                this.EvAg.bind("content:show", this.load);
                this.EvAg.bind("content:hide", this.close);

            },
            events: {
                "click .fade":              "fadeClick",
                "click .item-nav-button":   "navClick"
            },
            navClick: function(ev){
                var action = $(ev.currentTarget).attr("act");
                this.EvAg.trigger('exhibit:'+ action, this.modelContent.id);
            },
            fadeClick: function(ev){
                if ($(ev.target).hasClass("fade") ){
                    this.close();
                }
            },
            load: function(options){
                var self = this;
                if(typeof(options) === "string"){
                    this.modelContent = new ModelStory({id: options}, { properties: this.properties });
                    this.modelContent.fetch({
                        success: function(model, xhr, response){
                            self.render();
                        }
                    })
                } else {
                    this.modelContent = options.modelContent;
                    this.hasNext = options.hasNext;
                    this.hasPrevious = options.hasPrevious;
                    this.render();
                }
            },
            render: function(){
                this.body.removeClass();
                switch( this.modelContent.get("contentType") ){
                    case "Photo":
                        this.photoView.load({ modelPhoto: this.modelContent });
                        break;
                    case "Story":
                        this.storyView.load({ modelStory: this.modelContent });
                        break;
                }
                $("body").addClass("noScroll");
                
                if (!this.hasNext) $("div[act='next']").hide();
                else $("div[act='next']").show();
                
                if (!this.hasPrevious) $("div[act='previous']").hide();
                else $("div[act='previous']").show();
                
                this.element.show();
            },
            close: function(){
                var self = this;
                this.element.fadeOut(function(){
                    $("body").removeClass("noScroll");
                    self.body.empty();
                });
                this.EvAg.trigger('hash:reset');
            }
        });
    }
);