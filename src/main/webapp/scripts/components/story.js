define(
    [
        'jquery',
        'backbone',
        'underscore',
        'text!templates/story.html',
        'components/utils'
    ],
    function($, Backbone, _, templateStory, utils){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this,'render', 'load','createComment', 'renderImage', 'imageClick', 'nextImage','navClick', 'login');
                this.el = options.el;
                this.element = $(this.el);
                this.properties = options.properties;
                this.EvAg = options.EvAg;
                this.EvAg.bind('story/show', this.load);
                this.EvAg.bind('user/login', this.login);
                this.locked = false;
            },
            events: {
                "click .echo-s-h-close" : "close",
                "click .comment-submit": "createComment",
                "click .login-button": "commentLogin",
                "click .echo-s-b-thumbnail": "imageClick",
                "click .echo-s-b-item": "nextImage",
                "click .story-nav-button": "navClick",
                "click a": "close"
            },
            login: function(echoedUser){
                var self = this;
                this.properties.echoedUser = echoedUser;

                this.element.find('.comment-login').fadeOut(function(){
                    self.element.find('.comment-submit').fadeIn();
                });
            },
            navClick: function(ev){
                var self = this;
                var target = $(ev.currentTarget);
                var action = target.attr("act");
                self.EvAg.trigger('exhibit/story/'+ action, self.data.story.id);
            },
            load: function(id){
                var self = this;
                utils.AjaxFactory({
                    url: self.properties.urls.api + "/api/story/" + id,
                    success: function(data){
                        self.data = data;
                        self.EvAg.trigger('pagetitle/update', data.story.title);
                        self.render();
                    }
                })();
            },
            render: function(){
                var template = _.template(templateStory);
                var self = this;
                self.element.html(template);
                self.chapters = {
                    array: [],
                    hash: {}
                };

                $.each(self.data.chapters, function(index,chapter){
                    var hash = {
                        chapter: chapter,
                        images: []
                    };
                    if(index === 0){
                        hash.images.push(self.data.story.image);
                    }
                    $.each(self.data.chapterImages, function(index, chapterImage){
                        if(chapterImage.chapterId === chapter.id) {
                            hash.images.push(chapterImage.image);
                        }
                    });
                    if(hash.images.length === 0){
                        hash.images.push(self.data.story.image);
                    }
                    self.chapters.array.push(hash);
                    self.chapters.hash[chapter.id] = index;
                });
                self.currentChapterIndex = 0;
                self.currentImageIndex = 0;

                self.text = self.element.find('.echo-s-b-text');
                self.element.find('.echo-s-h-t-t').text(self.data.story.title);
                self.element.find('.echo-s-h-i-i').attr("src",self.data.story.image.storyUrl);
                if(self.properties.echoedUser !== undefined){
                    if(self.data.echoedUser.id === self.properties.echoedUser.id){
                        var header = self.element.find('.echo-story-header');
                        $('<a class="story-edit-button"></a></span>').text('Edit Story').attr("href","#write/story/" + self.data.story.id).appendTo(header);
                    }
                }
                self.gallery = self.element.find('.echo-s-b-gallery');
                self.userNode = self.element.find('.echo-s-h-t-n');
                var userLink = $('<a class="link-black bold-link"  href="#user/' + self.data.echoedUser.id + '"></a>')
                                    .text(self.data.echoedUser.name);

                if(self.properties.isWidget !== true){
                    var fromLink = $('<span style="display: block;"></span>').append(document.createTextNode('from '));
                    if(self.data.story.partnerHandle !== "Echoed"){
                        var p = self.data.story.partnerHandle ? self.data.story.partnerHandle : self.data.story.partnerId;
                        fromLink.append($('<a class="link-black bold-link"></a>').attr("href","#partner/" + p).text(self.data.story.productInfo));
                        fromLink.append('(<a target="_blank" class="echo-s-h-t-n-t-l" href="' + self.properties.urls.api + "/redirect/partner/" + self.data.story.partnerId + '">' + 'Visit Website' + '</a>)');
                    } else if (utils.isUrl(self.data.story.productInfo)){
                        fromLink.append($('<a class="link-black bold-link"  target="_blank" href="' + utils.makeUrl(self.data.story.productInfo) + '"></a>').text(self.data.story.productInfo));
                    } else {
                        fromLink.text(self.data.story.productInfo);
                    }
                }
                var userImage = $('<img />').attr("src", utils.getProfilePhotoUrl(self.data.echoedUser)).addClass("echo-s-h-t-n-i");
                self.userNode.append(userImage);

                self.itemNode = $("<div class='echo-s-b-item'></div>");
                self.itemImageContainer = $("<div class='echo-s-b-i-c'></div>");
                self.userTextNode = $("<div class='echo-s-h-t-n-t'></div>");
                self.userTextNode.append(userLink).prepend("by ").append(fromLink).appendTo(self.userNode);
                self.img = $("<img />");
                self.itemNode.append(self.itemImageContainer.append(self.img)).appendTo(self.gallery);
                self.galleryNode = $("#echo-story-gallery");
                //self.element.find('.echo-story-chapter-title').text(self.data.chapters[0].title);
                self.text.append($("<div class='echo-s-b-t-b'></div>"));
                self.renderGalleryNav();
                self.renderComments();
                self.renderChapter();

                self.EvAg.trigger('fade/show');
                self.element.css({
                    "margin-left": -(self.element.width() / 2)
                });
                self.element.fadeIn();
            },
            renderGalleryNav: function(){
                var self = this;
                self.thumbnails = {};
                self.titles = [];
                self.galleryChapters = [];
                $.each(self.chapters.array, function(index, chapter){
                    self.galleryChapters[index]=  $('<div></div>').addClass('echo-gallery-chapter');
                    var title = $('<div></div>').addClass('echo-gallery-title').text(chapter.chapter.title);
                    self.galleryChapters[index].append(title);
                    self.galleryNode.append(self.galleryChapters[index]);
                    $.each(chapter.images, function(index2, image){
                        var thumbNailHash = index + "-" + index2;
                        self.thumbnails[thumbNailHash] = $('<img />').addClass("echo-s-b-thumbnail").attr("index", thumbNailHash).attr("src", image.preferredUrl).css(utils.getImageSizing(image, 90));
                        self.galleryChapters[index].append(self.thumbnails[thumbNailHash]);
                    });
                });
            },
            nextImage: function(){
                var self = this;
                self.currentImageIndex++;
                if(self.currentImageIndex >= self.chapters.array[self.currentChapterIndex].images.length){
                    self.nextChapter();
                } else {
                    self.renderImage(self.currentImageIndex);
                }
            },
            nextChapter: function(){
                var self = this;
                self.currentChapterIndex++;
                self.currentImageIndex = 0;
                if(self.currentChapterIndex >= self.chapters.array.length){
                    self.currentChapterIndex = 0;
                }
                self.renderChapter(self.currentChapterIndex);
            },
            imageClick: function(e){
                var self = this;
                var index = $(e.target).attr("index");
                self.currentChapterIndex = index.split("-")[0];
                self.currentImageIndex = index.split("-")[1];
                self.renderChapter();
            },
            renderChapter: function(){
                var self = this;
                var textArea = self.element.find('.echo-s-b-text');
                textArea.fadeOut(function(){
                    self.element.find('.echo-story-chapter-title').text(self.chapters.array[self.currentChapterIndex].chapter.title);
                    //appending as text obviously breaks the link replacement...
                    self.element.find('.echo-s-b-t-b').html(utils.replaceUrlsWithLink(utils.escapeHtml(self.chapters.array[self.currentChapterIndex].chapter.text)).replace(/\n/g, '<br />'));
//                    self.element.find('.echo-s-b-t-b').html(utils.replaceUrlsWithLink(self.chapters.array[self.currentChapterIndex].chapter.text.replace(/\n/g, '<br />')));
                    textArea.fadeIn();
                });

                self.galleryNode.find('.echo-gallery-chapter').removeClass("highlight");
                self.galleryChapters[self.currentChapterIndex].addClass("highlight");
                self.renderImage();
            },
            renderImage: function(){
                var self = this;
                var currentImage = self.chapters.array[self.currentChapterIndex].images[self.currentImageIndex];
                self.img.fadeOut();
                self.itemImageContainer.animate(
                    utils.getImageSizing(currentImage, 400),
                    function(){
                        if(currentImage.storyUrl !== null){
                            self.img.attr('src', currentImage.storyUrl);
                        } else {
                            self.img.attr('src', currentImage.originalUrl);
                        }
                        self.img.fadeIn();
                    });

                self.galleryNode.find('.echo-s-b-thumbnail').removeClass("highlight");
                self.thumbnails[self.currentChapterIndex + "-" + self.currentImageIndex].addClass("highlight");
            },
            renderComments: function(){
                var self = this;
                var commentListNode = self.element.find('.echo-s-c-l');
                var comments = {
                    children: []
                };
                $.each(self.data.comments, function(index, comment){
                    var parentId = comment.parentCommentId;
                    if(parentId == null){
                        comments.children.push(comment);
                    } else {
                        if(comments[parentId] == null){
                            comments[parentId] = {
                                children: []
                            }
                        }
                        comments[parentId].children.push(comment);
                    }
                });
                commentListNode.empty();
                $("#echo-story-comment-ta").val("");
                if(self.data.comments.length > 0) $("#echo-s-c-t-count").text("(" + self.data.comments.length + ")");
                $.each(self.data.comments, function(index,comment){
                    var elapsedString = utils.timeElapsedString(utils.timeStampStringToDate(comment.createdOn.toString()));
                    var elapsedNode = $('<span class="echo-s-c-l-c-d"></span>').append(elapsedString);
                    var commentUserNode = $('<div class="echo-s-c-l-c-u"></div>').append($("<a class='red-link'></a>").text(comment.echoedUser.name).attr("href","#user/" + comment.echoedUser.id)).append(elapsedNode);
                    var img = $('<img class="echo-s-c-l-c-u-i" />').attr("src", utils.getProfilePhotoUrl(comment.echoedUser)).attr("align", "absmiddle");
                    img.prependTo(commentUserNode);
                    var commentText = $('<div class="echo-s-c-l-c-t"></div>').text(comment.text.replace(/\n/g, '<br />'));
                    var commentNode = $('<div class="echo-s-c-l-c"></div>').append(commentUserNode).append(commentText);
                    commentListNode.append(commentNode);
                });
                if(Echoed.echoedUser) {
                    self.element.find('.comment-submit').fadeIn();
                } else{
                    self.element.find('.comment-login-fb').attr("href", utils.getFacebookLoginUrl("redirect/close"));
                    self.element.find('.comment-login-tw').attr("href", utils.getTwitterLoginUrl("redirect/close"));
                    self.element.find('.comment-login').fadeIn();
                }
            },
            commentLogin: function(ev){
                var href = $(ev.currentTarget).attr("href");
                var child = window.open("about:blank", "Echoed",'width=800,height=440,toolbar=0,menubar=0,location=0,status=1,scrollbars=0,resizable=0,left=0,top=0');
                child.location = href;
            },
            createComment: function(){
                var self = this;
                var storyId = self.data.story.id;
                var chapterId = self.data.chapters[0].id;
                var text = $.trim($("#echo-story-comment-ta").val());
                if(text === ""){
                    alert("Please enter in a comment");
                } else if(self.locked !== true){
                    self.locked = true;
                    utils.AjaxFactory({
                        url: self.properties.urls.api + "/story/" + storyId + "/chapter/" + chapterId + "/comment",
                        type: "POST",
                        data: {
                            text: text,
                            storyOwnerId: self.data.echoedUser.id
                        },
                        success: function(createCommentData) {
                            self.locked = false;
                            self.data.comments.push(createCommentData);
                            self.renderComments();
                        }
                    })();
                }
            },
            close: function(){
                this.element.fadeOut();
                this.EvAg.trigger("fade/hide");
                this.EvAg.trigger("hash/reset");
            }
        });

    }
);
