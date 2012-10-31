define(
    [
        'jquery',
        'backbone',
        'underscore',
        'text!templates/mobile/story/story.html',
        'text!templates/mobile/story/chapter.html',
        'components/utils'
    ],
    function($, Backbone, _, templateStory, templateChapter, utils){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.el = options.el;
                this.element = $(this.el);
                this.properties = options.properties;
                this.EvAg = options.EvAg;
                if(this.properties.isWidget === true){
                    this.EvAg.bind("user/login", this.login);
                }
                this.EvAg.bind('story/change', this.storyChange);
                this.EvAg.bind('page/show', this.pageLoad);
                this.EvAg.bind('story/show', this.load);
                this.EvAg.bind('user/login', this.login);
                this.locked = false;
            },
            events: {
                "click .comment-submit": "createComment",
                "click .upvote": "upVote",
                "click .downvote": "downVote",
                "swipeleft": "nextStory",
                "swiperight": "previousStory",
                "click #echo-story-gallery-next": "next",
                "click #echo-story-gallery-prev": "previous",
                "click #story-follow": "followClick",
                "click #comments-login": "loginClick",
                "click .echo-chapter": "chapterClick"
            },
            chapterClick: function(ev){
                var target = $(ev.currentTarget);
                if(target.attr("index") != this.currentChapter){
                    $('.echo-story-chapter-body').hide();
                    target.find('.echo-story-chapter-body').slideDown();
                    var position = $("html").scrollTop() + target.offset().top;
                    console.log(position)
                    $("html, body").animate({ scrollTop : position });
                    this.currentChapter = target.attr("index");
                }

            },
            nextStory: function(){
                this.showDirection = 'right';
                this.hideDirection = 'left';
                this.EvAg.trigger('exhibit/story/next', this.data.story.id);
            },
            previousStory: function(){
                this.showDirection = 'left';
                this.hideDirection = 'right';
                this.EvAg.trigger('exhibit/story/previous', this.data.story.id);
            },
            storyChange: function(id){
                var self = this;
                self.element.hide('slide', { direction: this.hideDirection }, 250,
                    function(){
                        self.load(id, function(){
                            self.element.show('slide', { direction: this.showDirection }, 250 );
                        })
                    }
                )
            },
            pageLoad: function(options){
                if(options.indexOf('story') >= 0){
                    this.element.fadeIn();
                } else {
                    this.element.fadeOut();
                }
            },
            loginClick: function(){
                this.EvAg.trigger("login/show","story");
                this.element.fadeOut();
            },
            followClick: function(ev){
                var self = this;
                var request = {};
                var currentTarget = $(ev.currentTarget);
                var followId = currentTarget.attr("echoedUserId");
                if(self.properties.echoedUser !== undefined ){
                    if(self.properties.echoedUser.id !== followId) {
                        if(self.following === false){
                            request = {
                                url: self.properties.urls.api + "/api/me/following/" + followId,
                                type: "PUT",
                                success: function(data){
                                    self.following = true;
                                    currentTarget.text("Unfollow").addClass("redButton").removeClass("greyButton");
                                }
                            }
                        } else {
                            request = {
                                url: self.properties.urls.api + "/api/me/following/" + followId,
                                type: "DELETE",
                                success: function(data){
                                    self.following = false;
                                    currentTarget.text("Follow").removeClass("redButton").addClass("greyButton");
                                }
                            }
                        }
                        utils.AjaxFactory(request)();
                    }
                } else{
                    self.showLogin();
                }
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
                var target = $(ev.currentTarget);
                if(self.properties.echoedUser !== undefined){
                    utils.AjaxFactory({
                        url: self.properties.urls.api + "/api/upvote",
                        data: {
                            storyId: self.data.story.id,
                            storyOwnerId: self.data.echoedUser.id
                        },
                        success: function(data){
                            self.data.votes[self.properties.echoedUser.id] = {
                                echoedUserId: self.properties.echoedUser.id,
                                value: 1
                            };
                            self.renderVotes();
                        }
                    })();
                } else{
                    self.showLogin();
                }
            },
            downVote: function(ev){
                var self = this;
                var target = $(ev.currentTarget);
                if(self.properties.echoedUser !== undefined){
                    utils.AjaxFactory({
                        url: self.properties.urls.api + "/api/downvote",
                        data: {
                            storyId: self.data.story.id,
                            storyOwnerId: self.data.echoedUser.id
                        },
                        success: function(data){
                            self.data.votes[self.properties.echoedUser.id] = {
                                echoedUserId: self.properties.echoedUser.id,
                                value: -1
                            };
                            self.renderVotes();
                        }
                    })();
                }
                else {
                    self.showLogin();
                }
            },
            login: function(echoedUser){
                var self = this;
                this.properties.echoedUser = echoedUser;
                this.element.find('.comment-login').fadeOut(function(){
                    self.element.find('.comment-submit').fadeIn();
                });
                $('#story-login-container').fadeOut();
                self.renderVotes();
                self.renderFollowing();
            },
            navClick: function(ev){
                var self = this;
                var target = $(ev.currentTarget);
                var action = target.attr("act");
                self.EvAg.trigger('exhibit/story/'+ action, self.data.story.id);
            },
            load: function(id, callback){
                var self = this;
                utils.AjaxFactory({
                    url: self.properties.urls.api + "/api/story/" + id,
                    success: function(data){
                        self.data = data;
                        self.EvAg.trigger('pagetitle/update', data.story.title);
                        self.render();
                        if(callback !== undefined) callback();
                    }
                })();
            },
            renderViews: function(){
                var self = this;
                $('#story-views').text("Views: " + self.data.story.views);
            },
            renderVotes: function(){
                var self = this;
                var upVotes = 0, downVotes = 0, key;
                var votes = self.data.votes;
                self.element.find('.upvote').removeClass('on');
                self.element.find('.downvote').removeClass('on');
                for (key in votes){
                    if(votes[key].value > 0 ) upVotes++;
                    else if(votes[key].value < 0) downVotes++;
                }
                $('#upvote-counter').text(upVotes);
                $('#downvote-counter').text(downVotes);

                if(self.properties.echoedUser !== undefined){
                    var vote = self.data.votes[self.properties.echoedUser.id];
                    if(vote !== undefined){
                        if(vote.value > 0) self.element.find('.upvote').addClass('on');
                        else if(vote.value < 0) self.element.find('.downvote').addClass('on');
                    }
                }
            },
            renderFollowing: function(){
                var self = this;
                self.following = false;
                $('#story-follow').attr("echoedUserId", self.data.echoedUserId);
                if(self.properties.echoedUser !== undefined){
                    if(self.properties.echoedUser.id !== self.data.echoedUser.id){
                        utils.AjaxFactory({
                            url: self.properties.urls.api + "/api/me/following",
                            success: function(data){

                                $.each(data, function(index, following){
                                    if(following.echoedUserId === self.data.echoedUser.id ) self.following = true;
                                });
                                if(self.following === true){
                                    $('#story-follow').text("Unfollow").removeClass("greyButton").addClass("redButton").fadeIn();
                                } else {
                                    $('#story-follow').addClass('greyButton').text("Follow").fadeIn();
                                }
                            }
                        })();
                    } else{
                        $('#story-follow').fadeOut();
                    }
                } else {
                    $('#story-follow').addClass('greyButton').text("Follow").fadeIn();
                }
            },
            render: function(){

                var self = this;
                var template = _.template(templateStory, self.data);
                self.element.html(template);

                self.text = self.element.find('.echo-s-b-text');
                if(self.properties.echoedUser !== undefined){
                    if(self.data.echoedUser.id === self.properties.echoedUser.id){
                        var header = self.element.find('.echo-story-header');
                        $('#story-edit-tab').attr("href","#write/story/" + self.data.story.id).css("display","inline-block");
                    }
                }

                self.gallery = self.element.find('.echo-s-b-gallery');

                $('#echo-s-h-t-n-i').attr("src", utils.getProfilePhotoUrl(self.data.echoedUser, self.properties.urls));
                $('#story-follow').attr("echoedUserId", self.data.echoedUser.id);
                if(self.properties.isWidget) $('#story-user-link').attr("href", self.properties.urls.api + "#user/" + self.data.echoedUser.id).attr('target',"_blank");
                else $('#story-user-link').attr("href", self.properties.urls.api + "#user/" + self.data.echoedUser.id)

                if(self.properties.isWidget !== true && self.data.story.productInfo !== null){
                    var fromLink = $('<div class="echo-story-from"></div>');
                    if(self.data.story.partnerHandle !== "Echoed"){
                        var p = self.data.story.partnerHandle ? self.data.story.partnerHandle : self.data.story.partnerId;
                        fromLink.append($('<a class="story-link link-white bold-link"></a>').attr("href","#partner/" + p).text(self.data.story.productInfo));
                        fromLink.append(' (<a target="_blank" class="echo-s-h-t-n-t-l" href="' + self.properties.urls.api + "/redirect/partner/" + self.data.story.partnerId + '">' + 'Visit Website' + '</a>)');
                    } else if (utils.isUrl(self.data.story.productInfo)){
                        fromLink.append($('<a class="link-white bold-link"  target="_blank" href="' + utils.makeUrl(self.data.story.productInfo) + '"></a>').text(self.data.story.productInfo));
                    } else {
                        fromLink.text(self.data.story.productInfo);
                    }
                    self.element.find('.echo-s-h-title').append(fromLink);
                }

                self.mainBody = $('#echo-story-main');
                self.galleryNode = $("#echo-story-gallery");
                self.renderComments();
                self.renderChapters();
                self.renderFollowing();
                self.renderViews();
                $('#echo-chapter-0').find('.echo-story-chapter-body').show();
                self.currentChapter = 0;
            },
            renderChapters: function(){
                var self = this;
                self.chapters = [];
                $.each(self.data.chapters, function(index, chapter){
                    var template = _.template(templateChapter, chapter);
                    var c = $('<div></div>').addClass('echo-chapter').appendTo(self.mainBody).attr('id', "echo-chapter-" + index).attr("index", index);
                    self.chapters[index] = c;
                    c.html(template);
                    var gallery = c.find('.echo-story-chapter-gallery');
                    if(index === 0 && self.data.story.image !== null){
                        $('<img />').attr("src", self.data.story.image.preferredUrl).addClass('echo-story-chapter-image').appendTo(gallery);
                    }
                    $.each(self.data.chapterImages, function(index, chapterImage){
                        if(chapterImage.chapterId === chapter.id){
                            $('<img />').attr("src", chapterImage.image.preferredUrl).addClass('echo-story-chapter-image').appendTo(gallery);
                        }
                    });
                });
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


                $.each(self.data.comments, function(index,comment){
                    var elapsedString = utils.timeElapsedString(utils.timeStampStringToDate(comment.createdOn.toString()));
                    var elapsedNode = $('<span class="echo-s-c-l-c-d"></span>').append(elapsedString);
                    var commentUserNode = $('<div class="echo-s-c-l-c-u"></div>').append($("<a class='story-link red-link'></a>").text(comment.echoedUser.name).attr("href","#user/" + comment.echoedUser.id)).append(elapsedNode);
                    var img = $('<img class="echo-s-c-l-c-u-i" />').attr("src", utils.getProfilePhotoUrl(comment.echoedUser, self.properties.urls)).attr("align", "absmiddle");
                    img.prependTo(commentUserNode);
                    var commentText = $('<div class="echo-s-c-l-c-t"></div>').html(utils.replaceUrlsWithLink(utils.escapeHtml(comment.text).replace(/\n/g, '<br />')));
                    var commentNode = $('<div class="echo-s-c-l-c"></div>').append(commentUserNode).append(commentText);
                    commentListNode.append(commentNode);
                });
                $('#echo-s-c-t-count').text("(" + self.data.comments.length + ")");
                if(self.properties.echoedUser) self.element.find('.comment-submit').fadeIn();
                else self.element.find('.comment-login').fadeIn();
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
            hide: function(){
                this.element.hide();
                this.element.empty();
            }
        });

    }
);
