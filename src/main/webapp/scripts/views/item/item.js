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
                this.EvAg =         options.EvAg;
                this.element =      $(options.el);
                this.properties =   options.properties;

                this.element.html(tmpItem());
                this.body =  $('#item-body');

                this.EvAg.bind( "content:show", this.load );

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
                    this.render();
                }
            },
            render: function(){
                this.body.removeClass();
                switch( this.modelContent.get("_type") ){
                    case "photo":
                        this.contentView = new PhotoView({ el: this.body, modelPhoto: this.modelContent, modelUser: this.modelUser, EvAg: this.EvAg, properties: this.properties });
                        break;
                    case "story":
                        this.contentView = new StoryView({ el: this.body, modelStory: this.modelContent, modelUser: this.modelUser, EvAg: this.EvAg, properties: this.properties })
                }
                $("body").addClass("noScroll");
                this.element.show();
            },
            close: function(){
                var self = this;
                this.element.fadeOut(function(){
                    $("body").removeClass("noScroll");
                    self.body.empty();
                });

            }
        });
    }
);