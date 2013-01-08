define(
    [
        'backbone',
        'components/utils'
    ],
    function(Backbone, utils) {
        return Backbone.Model.extend({
            initialize: function(attr, options){
                if(attr === null) {
                    this.initStory(options);
                    this.properties = options.properties;
                } else {
                    this.properties = options.properties;
                    this.url = options.properties.urls.site + "/api/story/" + attr.id
                    this.currentChapterIndex = 0;
                    this.currentChapterImageIndex = 0;
                }
            },
            initStory: function(options){
                var self = this;
                utils.AjaxFactory({
                    url: options.properties.urls.site + "/story",
                    data: options.loadData,
                    dataType: 'jsonp',
                    success: function(response){
                        self.set(response.storyFull);
                        self.set("partner", response.partner);
                        options.success(self)
                    }
                })();
            },
            submitCover: function(options, callback){
                var self = this;
                var type = "POST";
                var url = this.properties.urls.site + "/story";
                if(this.get("isNew") === false){
                    type = "PUT";
                    url += "/" + this.id;
                }
                utils.AjaxFactory({
                    type: type,
                    url: url,
                    data: options,
                    success: function(response){
                        self.set("story", response);
                        self.set("isNew", false);
                        callback(self)
                    }
                })();
            },
            moderate: function(baseUrl) {
                var id = this.get("id");
                var storyOwnerId = this.get("echoedUser").id;
                var isModerated = !this.get("isModerated");
                this.set("isModerated", isModerated);
                utils.AjaxFactory({
                    url: baseUrl + "/story/" + id + "/moderate",
                    type: "POST",
                    data: {
                        moderated: isModerated,
                        storyOwnerId : storyOwnerId
                    },
                    success: function(data){
                    }
                })();
            },
            upVote: function(callback){
                var self = this;
                utils.AjaxFactory({
                    url: this.properties.urls.site + "/api/upvote",
                    data: {
                        storyId: this.id,
                        storyOwnerId: this.get("echoedUser").id
                    },
                    success: function(){
                        callback(self)
                    }
                })();
            },
            downVote: function(callback){
                var self = this;
                utils.AjaxFactory({
                    url: this.properties.urls.site + "/api/downvote",
                    data: {
                        storyId: this.id,
                        storyOwnerId: this.get("echoedUser").id
                    },
                    success: function(){
                        callback(self)
                    }
                })();
            },
            getChapterImages: function(chapterId){
                var chapterImages = this.get("chapterImages");
                var i = [];
                $.each(chapterImages, function(index, image){
                    if(image.chapterId === chapterId) i.push(image);
                });
                return i;
            },
            getChapter: function(index){
                var chapters = this.get("chapters");
                if(chapters.length > index ) return this.get("chapters")[index];
                return {};
            },
            saveChapter: function(options, callback){
                var self = this;
                var type = "POST";
                var url = this.properties.urls.api + "/story/" + this.id + "/chapter";
                if(options.chapterId) {
                    url += "/" + options.chapterId;
                    type = "PUT";
                }
                utils.AjaxFactory({
                    url: url,
                    type: type,
                    processData: false,
                    contentType: "application/json",
                    data: JSON.stringify(options),
                    success: function(response){
                        self.updateChapter(response.chapter, response.chapterImages);
                        callback(self, response)
                    }
                })();
            },
            saveComment: function(text, callback){
                var self = this;
                var chapterId = this.getChapter(0).id;
                var url = this.properties.urls.api + "/story/" + this.id + "/chapter/" + chapterId + "/comment";
                utils.AjaxFactory({
                    type: "POST",
                    url: url,
                    data: {
                        text: text,
                        storyOwnerId: this.get("echoedUser").id
                    },
                    success: function(response){
                        var comments = self.get("comments");
                        comments.push(response);
                        self.set("comments", comments);
                        callback(self, response);
                    }
                })();
            },
            updateChapter: function(chapter, chapterImages){
                var chapters = this.get("chapters");
                var newImages = [];
                $.each(chapters, function(index, c){
                    if(c.id === chapter.id) chapters[index] = chapter;
                });
                $.each(this.get("chapterImages"), function(index, ci){
                    if(ci.chapterId !== chapter.id) newImages.push(ci);
                });
                $.each(chapterImages, function(index, ci){
                    newImages.push(ci);
                });
                this.set("chapters", chapters);
                this.set("chapterImages", newImages);
            },
            isIncomplete: function(){
                return this.get("chapters").length == 0
            },
            getImageCount: function(){
                return 0;
            },
            getCoverImage: function(){
                if(this.get("story").image) return this.get("story").image;
                else if (this.get("chapterImages").length > 0) return this.get("chapterImages")[0].image;
                else return null;
            },
            getCurrentChapter: function(){
                return this.get("chapters")[this.currentChapterIndex];
            },
            getCurrentImage: function(){
                var chapterImages = this.getChapterImages(this.getCurrentChapter().id);
                return this.get("chapterImages")[this.currentChapterImageIndex].image;
            },
            nextChapter: function(){
                var chapters = this.get("chapters");
                this.currentChapterIndex++;
                if(this.currentChapterIndex >= chapters.length ) this.currentChapterIndex = 0;
                this.currentChapterImageIndex = 0;
            },
            nextImage: function(){
                var chapterImages = this.getChapterImages(this.getCurrentChapter().id);
                this.currentChapterImageIndex++;
                if(this.currentChapterImageIndex >= chapterImages.length) this.nextChapter();
            },
            setCurrentChapter: function(chapterIndex){
                this.currentChapterIndex = chapterIndex;
                this.currentChapterImageIndex = 0;
            },
            setCurrentImage: function(chapterIndex, imageIndex){
                this.currentChapterIndex = chapterIndex;
                this.currentChapterImageIndex = imageIndex;
            }
        });
    }
);