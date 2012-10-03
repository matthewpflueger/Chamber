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

                var textNode = self.element.find(".story-brief-text");

                if(self.data.chapters.length > 0){
                    var chapterText = self.data.chapters[0].text;
                    var c  = chapterText.split(/[.!?]/)[0];
                    c = c + chapterText.substr(c.length, 1); //Append Split Character
                    self.data.chapterText = c;
                } else {
                    textNode.append("<strong><span class='highlight'>Your Story is Incomplete. Please add a topic.</span></strong><br/>");
                    self.data.chapterText = "";
                }

                var dateString = self.data.story.updatedOn.toString();
                self.data.elapsedString = utils.timeElapsedString(utils.timeStampStringToDate(dateString));
                self.data.profilePhotoUrl = utils.getProfilePhotoUrl(self.data.echoedUser);
                var template = _.template(templateStoryBrief, self.data);
                self.element.html(template);


                var image = self.data.story.image;
                var imageNode = self.element.find(".story-brief-image");
                if(image !== null){
                    self.data.storyImage = {
                        url: image.preferedUrl,
                        size: utils.getImageSizing(image, 260)
                    };
                    imageNode.attr('src', image.preferredUrl).css(utils.getImageSizing(image,260))
                } else{
                    self.element.find('.story-brief-image-container').hide();
                }


                if(self.properties.echoedUser !== undefined){
                    if(self.data.echoedUser.id === Echoed.echoedUser.id){
                        self.element.find('.story-brief-edit').show();
                    }
                }

                self.element.attr("id", self.data.story.id);
            },
            showOverlay: function(){
            },
            hideOverlay: function(){
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
