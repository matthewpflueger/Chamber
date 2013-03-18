define(
    [
        'jquery',
        'backbone',
        'components/utils'
    ],
    function($, Backbone, utils) {
        return Backbone.Model.extend({
            initialize: function(attr, options){
                if(attr === null) {
                    this.initStory(options);
                    this.properties = options.properties;
                } else {
                    this.properties = options.properties;
                    if (options.properties) this.url = options.properties.urls.site + "/api/story/" + attr.id;
                    else this.url = "/api/story/" + attr.id;
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
                    success: function(response){
                        self.set("votes", response);
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
                    success: function(response){
                        self.set("votes", response);
                        callback(self)
                    }
                })();
            },
            isFirstChapter: function(chapterId){
                var chapters = this.get("chapters")
                if(chapters.length) return chapters[0].id === chapterId;
                else return false
            },
            getChapterImages: function(chapter, includeCover) {
                if (!chapter || !chapter.id) return [];

                var chapterId = chapter.id;
                var chapterImages = this.get("chapterImages");
                var i = [];
                //Fix for pushing in Cover Image
                if(this.isFirstChapter(chapterId) && includeCover && this.get("story").image) i.push({ image: this.get("story").image });

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
            saveLink: function(link, title, callback){
                var self = this;
                var url = this.properties.urls.api + "/story/" + this.id + "/link";
                utils.AjaxFactory({
                    type: "POST",
                    url: url,
                    data: {
                        url: link,
                        title: title
                    },
                    success: function(response){
                        var links = self.get("links");
                        links.push(response);
                        self.set("links", links);
                        callback(self, response);
                    }
                })();
            },
            updateChapter: function(chapter, chapterImages){
                var chapters = this.get("chapters");
                var newChapter = true;
                var newImages = [];
                $.each(chapters, function(index, c){
                    if(c.id === chapter.id) {
                        chapters[index] = chapter;
                        newChapter = false;
                    }
                });
                if(newChapter) chapters.push(chapter);
                $.each(this.get("chapterImages"), function(index, ci){
                    if(ci.chapterId !== chapter.id) newImages.push(ci);
                });
                $.each(chapterImages, function(index, ci){
                    newImages.push(ci);
                });
                this.set("chapters", chapters);
                this.set("chapterImages", newImages);
                this.set("isNew", false);
            },
            isIncomplete: function(){
                return this.get("chapters").length == 0
            },
            getTitle: function(){
                var title = this.get("story").title;
                if(title){
                    return title;
                } else {
                    var chapter = this.get("chapters")[0];
                    if(chapter.title){
                        return chapter.title;
                    } else {
                        return chapter.text;
                    }
                }
            },
            getImageCount: function(){
                return 0;
            },
            getCoverImage: function(){
                if(this.get("story").image) return this.get("story").image;
                else if (this.get("chapterImages").length > 0) return this.get("chapterImages")[0].image;
                else return null;
            },
            getCurrentChapterIndex: function(){
                return this.currentChapterIndex;
            },
            getCurrentChapterImageIndex: function(){
                return this.currentChapterImageIndex;
            },
            getCurrentChapter: function(){
                return this.get("chapters")[this.currentChapterIndex];
            },
            getCurrentImage: function(includeCover){
                var chapterImages = this.getChapterImages(this.getCurrentChapter(), includeCover);
                if (chapterImages.length) return chapterImages[this.currentChapterImageIndex].image;
                else return false;
            },
            nextChapter: function(){
                var chapters = this.get("chapters");
                this.currentChapterIndex++;
                if(this.currentChapterIndex >= chapters.length ) this.currentChapterIndex = 0;
                this.currentChapterImageIndex = 0;
            },
            nextImage: function(){
                var chapterImages = this.getChapterImages(this.getCurrentChapter());
                this.currentChapterImageIndex++;
                if (this.currentChapterImageIndex >= chapterImages.length) this.nextChapter();
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