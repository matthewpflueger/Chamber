define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'components/widget/messageDialog',
        'hgn!templates/remote/remote'
    ],
    function($, Backbone, _, utils, MessageDialog, templateRemote){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.element = $(this.el);
                this.properties = options.properties;
                this.element.addClass("ech-" + this.properties.remote.vert + "-" + this.properties.remote.hor);
                this.element.addClass("ech-" + this.properties.remote.orientation);
                this.render();
            },
            events: {
                "mouseenter #echoed-i": "info",
                "mouseenter #echoed-add": "add",
                "mouseenter .echoed-story": "story",
                "mouseenter #echoed-icon-overlay": "browse",
                "mouseleave .echoed-option": "leave",
                "click .echoed-story": "clickStory",
                "click #echoed-add": "clickAdd",
                "click #echoed-i": "clickInfo",
                "click #echoed-icon-overlay": "clickBrowse"
            },
            clickBrowse: function(ev){
                window.location.hash = '#echoed';
            },
            clickStory: function(ev){
                window.location.hash = $(ev.currentTarget).attr('href');
            },
            clickAdd: function(ev){
                window.location.hash = "#echoed_write";
            },
            clickInfo: function(ev){
                window.open(this.properties.urls.site + "/websites");
            },
            triggerPreview: function(msgObj){
                this.EvAg.trigger("preview/show", msgObj);
            },
            info: function(){
                this.triggerPreview({ "type": "text", "data": "What is this?" });
            },
            add: function(){
                this.triggerPreview({ "type": "text", "data": this.properties.widgetShareMessage });
            },
            browse: function(){
                this.triggerPreview({ "type": "text", "data": this.properties.widgetTitle })
            },
            leave: function(){
                this.EvAg.trigger("preview/leave");
            },
            story: function(ev){
                var index = $(ev.currentTarget).attr("index");
                var storyFull =this.stories[index];
                var msg = {
                    "type" : "story",
                    "data" : storyFull
                };
                this.triggerPreview(msg);
            },
            render: function(){
                var self = this;
                var template = templateRemote();
                this.element.html(template);
                this.options = $('#echoed-options');
                this.messageDialog = new MessageDialog({ el: "#echoed-preview", properties: this.properties, EvAg: this.EvAg});
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/api/partner/" + this.properties.partnerId,
                    dataType: 'jsonp',
                    success: function(response){
                        self.content = response.content;
                        self.storyIndex = 0;
                        var i = 0, counter = 0;
                        while(i < self.content.length && counter < 4){
                            var story = self.content[i];
                            if(story.story.image) {
                                var link = $('<div></div>').attr("href", "#echoed_story/" + story.id).append(utils.fit(story.story.image, 40 , 40)).attr('index', i).addClass("echoed-story").addClass('echoed-option');
                                $('#echoed-options').prepend(link);
                                counter++;
                            }
                            i++;
                        }
                    }
                })();
            }
        });
    });