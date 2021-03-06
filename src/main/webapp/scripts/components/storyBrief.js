define(
    [
        'jquery',
        'backbone',
        'underscore',
        'hgn!templates/storyBrief/storyBrief',
        'components/utils'
    ],
    function($, Backbone, _, templateStoryBrief, utils){
        return  Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.el =           options.el;
                this.element =      $(this.el);
                this.properties =   options.properties;
                this.modelUser =    options.modelUser;
                this.modelStory =   options.modelStory;
                this.EvAg =         options.EvAg;
                this.personal =     options.Personal;
                this.render();
            },
            events: {
                "click .item_content":  "click",
                "mouseenter":           "showOverlay",
                "mouseleave":           "hideOverlay"
            },
            render: function(){
                var self =          this;
                var image =         this.modelStory.getCoverImage();
                var chapterText =   "";
                if(this.modelStory.get("chapters").length){
                    chapterText =   this.modelStory.get("chapters")[0].text;
                    if(image !== null && image !== undefined){
                        var c =         chapterText.split(/[.!?]/)[0];
                        c = c +         chapterText.substr(c.length, 1); //Append Split Character
                        chapterText =   c;
                    } else {
                        var len =       300;
                        if(chapterText.length > len){
                            c =         chapterText.substr(len).split(/[.!?]/)[0];
                            c =         chapterText.substr(0, len) + c + chapterText.substr(len +c.length, 1);
                            chapterText = c;
                        }
                    }
                }

                var imageCount =        this.modelStory.getImageCount();
                var inComplete =        this.modelStory.isIncomplete();
                var profilePhotoUrl =   utils.getProfilePhotoUrl(this.modelStory.get("echoedUser"), this.properties.urls);
                var elapsedString =     utils.timeElapsedString(utils.timeStampStringToDate(this.modelStory.get("story").updatedOn.toString()));
                var isSelf =            this.modelUser.is(this.modelStory.get("echoedUser").id);
                var voteCount =         utils.arraySize(this.modelStory.get("votes"));

                var jsonModel = {
                    storyFull:          this.modelStory.toJSON(),
                    inComplete:         inComplete,
                    elapsedString:      elapsedString,
                    imageCount:         imageCount,
                    profilePhotoUrl:    profilePhotoUrl,
                    chapterText:        chapterText,
                    isWidget:           this.properties.isWidget,
                    isSelf:             isSelf,
                    voteCount:          voteCount
                };

                if(image !== null && image !== undefined){
                    var i = utils.scaleByWidth(image, 260);
                    jsonModel.storyImage = {
                        url: i.attr("src"),
                        height: i.attr("height"),
                        width: i.attr("width")
                    };
                }

                self.element.html(templateStoryBrief(jsonModel));
                self.element.addClass('item-story');

                if(self.properties.isWidget) self.element.find('.story-brief-text-user').attr("target","_blank").attr("href", self.properties.urls.api + "#user/" + this.modelStory.get("echoedUser").id);

                self.element.attr("id", this.modelStory.id);
            },
            showOverlay: function(){
                if(this.imageContainer) this.imageContainer.addClass('highlight');
            },
            hideOverlay: function(){
                if(this.imageContainer) this.imageContainer.removeClass('highlight');
            },
            click: function(ev){
                var target =    $(ev.target);
                if(!target.is('a')){
                    if(this.modelStory.get("chapters").length > 0){
                        Backbone.history.navigate("/story/" + this.modelStory.id)
                    } else {
                        Backbone.history.navigate("/write/" + this.modelStory.id)
                    }
                }
            }
        });
    }
)
