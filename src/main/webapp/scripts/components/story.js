define(
    [
        'jquery',
        'backbone',
        'underscore',
        'views/follow/follow',
        'models/story',
        'hgn!templates/story/story',
        'hgn!templates/story/comment',
        'components/utils'
    ],
    function($, Backbone, _, Follow, ModelStory, templateStory, templateComment, utils){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.el = options.el;
                this.element = $(this.el);
                this.properties = options.properties;
                this.EvAg = options.EvAg;
                this.modelUser = options.modelUser;
                this.EvAg.bind('story/show', this.load);
                this.EvAg.bind('story/hide', this.close);
                if(this.modelUser) this.modelUser.on("change:id", this.login);
                this.locked = false;
            },
            events: {
                "click .comment-submit": "createComment",
                "click .login-button": "commentLogin",
                "click .echo-gallery-chapter" : "chapterClick",
                "click .echo-s-b-thumbnail": "imageClick",
                "click #story-image-container": "nextImage",
                "click .story-nav-button": "navClick",
                "click .upvote": "upVote",
                "click .downvote": "downVote",
                "click #story-from": "fromClick",
                "click #echo-story-gallery-next": "next",
                "click #echo-story-gallery-prev": "previous",
                "click .story-share": "share",
                "click #comments-login": "showLogin",
                "click .story-link": "redirect",
                "click .fade" : "fadeClick"
            },
            fadeClick: function(ev){
                if($(ev.target).hasClass("fade")){
                    this.close();
                }
            },
            fromClick: function(ev){
                window.open(this.properties.urls.api + "/redirect/partner/" + this.modelStory.get("story").partnerId);
            },
            showLogin: function(){
                this.EvAg.trigger("login/init", "story/login");
            },
            share: function(ev){
                var target = $(ev.currentTarget);
                var href = "";
                switch(target.attr("type")){
                    case "fb":
                        href = "http://www.facebook.com/sharer/sharer.php?u=" + encodeURIComponent(this.properties.urls.api + "/graph/story/" + this.modelStory.id);
                        break;
                    case "tw":
                        href = "http://twitter.com/intent/tweet?original_referer="
                            + encodeURIComponent(this.properties.urls.api + "/#story/" + this.modelStory.id)
                            + "&url="
                            + encodeURIComponent(this.properties.urls.api + "/#story/" + this.modelStory.id)
                            + "&via="
                            + "echoedinc";
                        break;
                    case "pinterest":
                        href = "http://pinterest.com/pin/create/button?url="
                            + encodeURIComponent(this.properties.urls.api + "/#story/" + this.modelStory.id)
                            + "&media="
                            + encodeURIComponent(this.modelStory.getCoverImage().preferredUrl)
                            + "&description="
                            + encodeURIComponent(this.modelStory.get("story").title);
                        break
                }
                window.open(href, "Share",'width=800,height=440,toolbar=0,menubar=0,location=0,status=1,scrollbars=0,resizable=0,left=0,top=0');
            },
            next: function(){
                var self = this;
                self.scroll(self.galleryNode.scrollTop() + self.galleryNode.height());
            },
            previous: function(){
                var self = this;
                self.scroll(self.galleryNode.scrollTop() - self.galleryNode.height());
            },
            scroll: function(position){
                var self = this;
                self.galleryNode.animate({
                    scrollTop: position
                });
            },
            upVote: function(ev){
                var self = this;
                if(this.modelUser.isLoggedIn()){
                    this.modelStory.upVote(function(model){
                        self.renderVotes();
                    });
                } else{
                    self.showLogin();
                }
            },
            downVote: function(ev){
                var self = this;
                if(this.modelUser.isLoggedIn()){
                    this.modelStory.downVote(function(model){
                        self.renderVotes();
                    });
                } else{
                    self.showLogin();
                }

            },
            login: function(){
                var self = this;
                if(self.data){
                    this.element.find('.comment-login').fadeOut(function(){
                        self.element.find('.comment-submit').fadeIn();
                    });
                    $('#story-login-container').fadeOut();
                    self.renderVotes();
                }
            },
            navClick: function(ev){
                var self = this;
                var target = $(ev.currentTarget);
                var action = target.attr("act");
                self.EvAg.trigger('exhibit/story/'+ action, this.modelStory.id);
            },
            load: function(id){
                var self = this;
                this.modelStory = new ModelStory({ id: id }, { properties: this.properties });
                this.modelStory.fetch({
                    success: function(model, xhr, response){
                        self.render();
                    }
                });
            },
            renderVotes: function(){
                var self = this;
                var upVotes = 0, downVotes = 0, key;
                var votes = this.modelStory.get("votes");
                self.element.find('.upvote').removeClass('on');
                self.element.find('.downvote').removeClass('on');
                for (key in votes){
                    if(votes[key].value > 0 ) upVotes++;
                    else if(votes[key].value < 0) downVotes++;
                }
                $('#upvote-counter').text(upVotes);
                $('#downvote-counter').text(downVotes);
                if(this.modelUser.isLoggedIn()){
                    var vote = votes[this.modelUser.get("id")];
                    if(vote !== undefined){
                        if(vote.value > 0) self.element.find('.upvote').addClass('on');
                        else if(vote.value < 0) self.element.find('.downvote').addClass('on');
                    }
                }
            },
            render: function(){
                var self = this;

                var view = {
                    story: this.modelStory.toJSON(),
                    profilePhotoUrl: utils.getProfilePhotoUrl(this.modelStory.get("echoedUser"), this.properties.urls),
                    isWidget: this.properties.isWidget,
                    isMine: this.modelUser.is(this.modelStory.get("echoedUser").id),
                    userLink: this.properties.urls.api + "#user/" + this.modelStory.get("echoedUser").id
                };



                var template = templateStory(view);
                self.element.html(template);

                self.text = $('#story-text-container');
                self.chapterText = $("#story-text");

                self.follow = new Follow({ el: '#story-user-follow', properties: this.properties, modelUser: this.modelUser, followId: this.modelStory.get("echoedUser").id });

                self.gallery = $('#story-gallery');
                self.itemImageContainer = $('#story-image-container');
                self.img = $("#story-image");
                self.galleryNode = $("#echo-story-gallery");
                self.galleryNodeBody = $('#echo-story-gallery-body');

                self.story = $('#story');
                self.renderGalleryNav();
                self.renderComments();
                self.renderChapter();
                self.renderVotes();
                self.scroll(0);
                self.story.css({ "margin-left": -(self.story.width() / 2) });
                self.element.fadeIn();
                $("body").addClass("noScroll");
            },
            renderGalleryNav: function(){
                var self = this;
                var chapters = this.modelStory.get("chapters");
                self.thumbnails = {};
                self.titles = [];
                self.galleryChapters = [];
                $.each(chapters, function(index, chapter){
                    self.galleryChapters[index] = $('<div></div>').addClass('echo-gallery-chapter').attr("index", index);
                    var title = $('<div></div>').addClass('echo-gallery-title').text(chapter.title);
                    self.galleryChapters[index].append(title);
                    self.galleryNodeBody.append(self.galleryChapters[index]);
                    var chapterImages  = self.modelStory.getChapterImages(chapter.id, true);
                    $.each(chapterImages, function(index2, ci){
                        var thumbNailHash = index + "-" + index2;
                        self.thumbnails[thumbNailHash] = utils.scaleByWidth(ci.image, 90).addClass("echo-s-b-thumbnail").attr("index", thumbNailHash);
                        self.galleryChapters[index].append(self.thumbnails[thumbNailHash]);
                    });
                });
            },
            nextImage: function(){
                this.modelStory.nextImage();
                this.renderChapter();
            },
            nextChapter: function(){
                this.modelStory.nextChapter();
                this.renderChapter();
            },
            imageClick: function(e){
                var indices = $(e.target).attr("index").split("-");
                this.modelStory.setCurrentImage(indices[0], indices[1]);
                this.renderChapter();
            },
            chapterClick: function(e){
                var target = $(e.target);
                if(!target.is("img")){
                    this.modelStory.setCurrentChapter($(e.currentTarget).attr("index"));
                    this.renderChapter();
                }
            },
            renderChapter: function(){
                var self = this;
                var chapter = this.modelStory.getCurrentChapter();
                var chapterText = chapter.text;
                if (chapterText.length < 300) this.chapterType = 'photo';
                else this.chapterType = 'text';

                self.chapterText.fadeOut(function(){
                    $('#story-chapter-title').text(chapter.title);
                    self.chapterText.html(utils.replaceUrlsWithLink(utils.escapeHtml(chapter.text)).replace(/\n/g, '<br />'));
                    if(chapterText.length >0) self.chapterText.show();
                    else self.chapterText.hide();
                    self.chapterText.fadeIn();
                });

                //self.scroll(self.galleryNode.scrollTop() + self.galleryChapters[index].position().top);

                self.renderImage();
            },
            renderImage: function(){
                var self = this;
                var currentImage = this.modelStory.getCurrentImage(true);
                var imageSizing = {};
                var imageUrl = "";
                if(currentImage){
                    if(this.chapterType === 'photo') {
                        var i = utils.fit(currentImage, 842, 700);
                        imageSizing = {
                            width:i.attr('width'),
                            height:i.attr('height')
                        }
                        imageUrl = i.attr('src');
                        self.gallery.addClass("gallery-photo");
                        self.gallery.removeClass('gallery-text');
                        self.text.addClass('caption')
                    } else {
                        var i = utils.scaleByWidth(currentImage, 450);
                        imageSizing = {
                            width: i.attr('width'),
                            height: i.attr('height')
                        }
                        imageUrl = i.attr('src');
                        self.gallery.addClass("gallery-text");
                        self.gallery.removeClass('gallery-photo');
                        self.text.removeClass('caption')
                    }

                    self.gallery.show();
                    self.img.fadeOut(function(){
                        self.itemImageContainer.animate(
                            imageSizing,
                            'fast',
                            function(){
                                self.img.css(imageSizing).attr('src', imageUrl).fadeIn();
                            });
                    });

                } else{
                    self.gallery.addClass("gallery-text");
                    self.gallery.removeClass('gallery-photo');
                    self.chapterText.removeClass('caption');
                    self.gallery.hide();
                }
            },
            renderComments: function(){
                var self = this;
                var commentListNode = $('#story-comments-list').empty();
                $("#echo-story-comment-ta").val("");

                var comments = this.modelStory.get("comments");
                $('#echo-s-c-t-count').text("(" + comments.length + ")");

                $.each(comments, function(index,comment){
                    var view = {
                        elapsedString: utils.timeElapsedString(utils.timeStampStringToDate(comment.createdOn.toString())),
                        comment: comment,
                        profilePhotoUrl: utils.getProfilePhotoUrl(comment.echoedUser, self.properties.urls)
                    };
                    var template = templateComment(view);
                    commentListNode.append(template);
                });

                if(this.modelUser.isLoggedIn()) self.element.find('.comment-submit').fadeIn();
                else self.element.find('.comment-login').fadeIn();
            },
            commentLogin: function(ev){
                var href = $(ev.currentTarget).attr("href");
                var child = window.open("about:blank", "Echoed",'width=800,height=440,toolbar=0,menubar=0,location=0,status=1,scrollbars=0,resizable=0,left=0,top=0');
                child.location = href;
            },
            createComment: function(){
                var self = this;
                var text = $.trim($("#echo-story-comment-ta").val());
                if(text === ""){
                    alert("Please enter in a comment");
                } else if(self.locked !== true){
                    self.locked = true;
                    this.modelStory.saveComment(
                        text,
                        function(model){
                            self.locked = false;
                            self.renderComments();
                        }
                    );
                }
            },
            redirect: function(){
                var self = this;
                this.element.fadeOut(function(){
                    self.element.empty();
                });
            },
            close: function(){
                this.element.fadeOut();
                this.element.empty();
                $("body").removeClass("noScroll");
                this.EvAg.trigger("hash/reset");
            }
        });

    }
);
