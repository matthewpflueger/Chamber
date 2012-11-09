define(
    [
        'jquery',
        'backbone',
        'underscore',
        'text!templates/story-brief.html',
        'text!templates/story-brief-text.html',
        'components/utils'
    ],
    function($, Backbone, _, templateStoryBrief, templateStoryBriefText, utils){
        return  Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.el = options.el;
                this.element = $(this.el);
                this.properties = options.properties;
                this.EvAg = options.EvAg;
                this.personal = options.Personal;
                this.data = options.data;
                this.render();
            },
            events: {
                "click .item_content": "click",
                "mouseenter": "showOverlay",
                "mouseleave": "hideOverlay"
            },
            render: function(){
                var self = this;
                self.element.addClass('item-story');
                var textNode = self.element.find(".story-brief-text");

                if(self.data.chapters.length > 0){
                    var chapterText = self.data.chapters[0].text;
                    if(self.data.story.imageId !== null){
                        var c  = chapterText.split(/[.!?]/)[0];
                        c = c + chapterText.substr(c.length, 1); //Append Split Character
                        self.data.chapterText = c;
                    } else {
                        var len = 200;
                        if(chapterText.length > len){
                            c = chapterText.substr(len).split(/[.!?]/)[0];
                            c = chapterText.substr(0, len) + c + chapterText.substr(len +c.length, 1);
                            self.data.chapterText = c;
                        } else {
                            self.data.chapterText = chapterText;
                        }

                    }


                } else {
                    textNode.append("<strong><span class='highlight'>Your Story is Incomplete. Please add a topic.</span></strong><br/>");
                    self.data.chapterText = "";
                }

                var dateString = self.data.story.updatedOn.toString();
                self.data.elapsedString = utils.timeElapsedString(utils.timeStampStringToDate(dateString));
                self.data.profilePhotoUrl = utils.getProfilePhotoUrl(self.data.echoedUser, self.properties.urls);

                self.data.imageCount = self.data.chapterImages.length + (self.data.story.image ? 1 : 0);
                var image = function(){
                    if(self.data.story.image) return self.data.story.image;
                    else if (self.data.chapterImages.length > 0) return self.data.chapterImages[0].image;
                    else return null;
                }();



                self.element.find('.vote-counter').text(utils.arraySize(self.data.votes));

                if(image !== null){
                    self.element.html(_.template(templateStoryBrief, self.data));
                    var imageNode = self.element.find(".story-brief-image");
                    self.imageContainer = self.element.find('.story-brief-image-container');
                    self.data.storyImage = {
                        url: image.preferedUrl,
                        size: utils.getImageSizing(image, 260)
                    };
                    imageNode.attr('src', image.preferredUrl).css(utils.getImageSizing(image,260))
                } else{
                    self.element.html(_.template(templateStoryBriefText, self.data));
                }

                if(self.properties.echoedUser !== undefined){
                    if(self.data.echoedUser.id === Echoed.echoedUser.id){
                        self.element.find('.story-brief-edit').show();
                    }
                }

                if(self.properties.isWidget) self.element.find('.story-brief-text-user').attr("target","_blank").attr("href", self.properties.urls.api + "#user/" + self.data.echoedUser.id);

                self.element.attr("id", self.data.story.id);
            },
            showOverlay: function(){
                if(this.imageContainer) this.imageContainer.addClass('highlight');
            },
            hideOverlay: function(){
                if(this.imageContainer) this.imageContainer.removeClass('highlight');
            },
            click: function(ev){
                var self = this;
                var target = $(ev.target);
                if(!target.is('a')){
                    if(self.data.chapters.length > 0){
                        window.location.hash = "#!story/" + self.data.story.id;
                    } else {
                        window.location.hash = "#!write/story/" + self.data.story.id;
                    }
                }
            }
        });
    }
)
