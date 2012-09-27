define(
    [
        'jquery',
        'backbone',
        'underscore',
        'text!templates/story-brief.html',
        'components/utils'
    ],
    function($, Backbone, _, templateStoryBrief, utils){
        return  Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this,'render','click','hideOverlay','showOverlay');
                this.el = options.el;
                this.element = $(this.el);
                this.properties = options.properties;
                this.EvAg = options.EvAg;
                this.personal = options.Personal;
                this.data = options.data;
                this.render();
            },
            events: {
                "click .story-brief-image-container" : "click",
                "mouseenter": "showOverlay",
                "mouseleave": "hideOverlay"
            },
            render: function(){
                var self = this;
                var template = _.template(templateStoryBrief, self.data);
                self.element.html(template);
                var imageNode = self.element.find(".story-brief-image");
                var textNode = self.element.find(".story-brief-text");
                var overlayNode = self.element.find(".story-brief-overlay-wrap");
                self.overlay = self.element.find(".story-brief-overlay");
                var image = self.data.story.image;
                if(image.preferredUrl !== null){
                    imageNode.attr("src", image.preferredUrl).css(utils.getImageSizing(image, 260));
                } else {
                    imageNode.attr("src", image.url)
                }
                if(self.data.chapters.length > 0){
                    if(self.properties.echoedUser !== undefined){
                        if(self.data.echoedUser.id === Echoed.echoedUser.id){
                            self.element.find('.story-brief-edit').show();
                            //textNode.append("<a class='red-link underline' href='#write/story/" + self.data.story.id + "'>Edit Story</a>");
                        }
                    }
                    self.element.find('.story-brief-text-user-image').attr("src", utils.getProfilePhotoUrl(self.data.echoedUser));
                    var chapterText = self.data.chapters[0].text;
                    var c  = chapterText.split(/[.!?]/)[0];
                    c = c + chapterText.substr(c.length, 1); //Append Split Character
                    self.element.find('.story-brief-text-quote').text(c);
                    var dateString = self.data.story.updatedOn.toString();
                    var elapsedString = utils.timeElapsedString(utils.timeStampStringToDate(dateString));
                    self.element.find('.s-b-i-c').text(elapsedString);
                } else {
                    if(self.personal === true ) {
                        textNode.append("<strong></strong>").text(self.data.story.title).append("<br/>");
                        textNode.append("<strong><span class='highlight'>Your Story is Incomplete. Please add a topic.</span></strong><br/>");
                        if(self.data.chapters.length === 0 ){
                            var editButton = $('<div></div>').addClass("story-brief-overlay-edit-button").text("Complete Story");
                            overlayNode.append(editButton);
                            overlayNode.append("<br/>Complete your story by adding a chapter");
                            self.overlay.fadeIn();
                        }
                        self.element.attr('action','write');
                    }
                }
                self.element.attr("id", self.data.story.id);
            },
            showOverlay: function(){
                this.overlay.fadeIn();
            },
            hideOverlay: function(){
                this.overlay.fadeOut();
            },
            click: function(){
                var self = this;
                var id = self.element.attr("id");
                if(self.element.attr('action') === "write"){
                    window.location.hash = "#!write/story/" + self.data.story.id;
                } else {
                    window.location.hash = "#!story/" + self.data.story.id;
                }
            }
        });
    }
)
