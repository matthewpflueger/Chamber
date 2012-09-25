define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/storyBrief',
        'components/utils',
        'text!templates/login.html',
        'isotope'
    ],
    function($, Backbone, _, StoryBrief, utils, templateLogin, isotope ){
        return Backbone.View.extend({
            el: '#content',
            initialize: function(options){
                _.bindAll(this,'render','next','init', 'nextStory', 'previousStory', 'login');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.EvAg.bind('exhibit/init', this.init);
                this.EvAg.bind('infiniteScroll', this.next);
                this.EvAg.bind('exhibit/story/next', this.nextStory);
                this.EvAg.bind('exhibit/story/previous', this.previousStory);
                this.EvAg.bind('user/login', this.login);
                this.element = $(this.el);
                this.exhibit = $('#exhibit');
            },
            init: function(options){
                var self = this;
                self.personal = false;
                self.EvAg.trigger('infiniteScroll/on');
                this.jsonUrl = self.properties.urls.api + "/api/" + options.endPoint;
                this.personal = options.personal;
                this.contentTitle = options.title;
                utils.AjaxFactory({
                    url: self.jsonUrl,
                    dataType: 'json',
                    success: function(data){
                        self.nextPage = data.nextPage ? data.nextPage : null;
                        self.stories = {
                            array: [],
                            hash: {}
                        };
                        if (self.isotopeOn === true) {
                            self.exhibit.isotope("destroy")
                        }
                        self.exhibit.empty();
                        self.exhibit.isotope({
                            itemSelector: '.item_wrap,.no_filter',
                            masonry:{
                                //columnWidth: 5
                            }
                        });
                        self.isotopeOn = true;
                        var title = self.contentTitle;
                        if(data.partner) title = data.partner.name;
                        if(data.echoedUser && self.personal !== true)  title = data.echoedUser.name;
                        console.log("Title: " + title);
                        self.EvAg.trigger("title/update", { title: title});
                        self.EvAg.trigger("pagetitle/update", title);
                        if(!self.properties.echoedUser && self.properties.exhibitShowLogin === true) self.addLogin();
                        self.render(data);
                    }
                })();
            },
            login: function(){
                var self = this;
                self.exhibit.isotope('remove', $('#login'))
            },
            nextStory: function(storyId){
                var self = this;
                var index = self.stories.hash[storyId];
                if((index + 1) >= self.stories.array.length){
                    self.next();
                }
                if((index + 1) < self.stories.array.length){
                    window.location.hash = "#story/" + self.stories.array[index + 1];
                }
            },
            previousStory: function(storyId){
                var self = this;
                var index = self.stories.hash[storyId];
                if(index> 0){
                    window.location.hash = "#story/" + self.stories.array[index - 1];
                }
            },
            render: function(data){
                var self = this;
                if(self.addStories(data) || self.addFriends(data)){
                    self.EvAg.trigger('infiniteScroll/unlock');
                }
            },
            next: function(){
                var self = this;
                if(self.nextPage !== null){
                    self.EvAg.trigger('infiniteScroll/lock');
                    var url = self.jsonUrl + "?page=" + (self.nextPage);
                    self.nextPage = null;
                    utils.AjaxFactory({
                        url: url,
                        success: function(data){
                            if(data.nextPage !== null) {
                                self.nextPage = data.nextPage;
                            }
                            self.render(data);
                        }
                    })();
                }
            },
            addLogin: function(){
                var self = this;
                self.loginDiv = $('<div></div>').addClass('item_wrap').attr("id","login");
                var template = _.template(templateLogin);
                self.loginDiv.html(template);
                self.loginDiv.find("#facebookLogin").attr("href", utils.getFacebookLoginUrl(window.location.hash));
                self.loginDiv.find("#twitterLogin").attr("href", utils.getTwitterLoginUrl(window.location.hash));
                self.exhibit.isotope('insert', self.loginDiv)
            },
            addFriends: function(data){
                var self = this;
                var friendsFragment = $('<div></div>');
                var friendsAdded = false;
                if(data.friends){
                    $.each(data.friends, function(index, friend){
                        var friendImage = $('<div class="friend-img"></div>');
                        var friendText = $('<div class="friend-text"></div>').text(friend.name);
                        var  a = $('<a></a>').attr("href","#user/" + friend.toEchoedUserId).addClass('item_wrap').addClass("friend");
                        $('<img />').attr("height","50px").attr("src",utils.getProfilePhotoUrl(friend)).appendTo(friendImage);
                        $('<div></div>').append(friendImage).append(friendText).appendTo(a);
                        friendsFragment.append(a);
                        friendsAdded = true;
                    });
                    self.exhibit.isotope('insert', friendsFragment.children());
                }
            },
            addStories: function(data){
                var self = this;
                var storiesFragment = $('<div></div>');
                var storiesAdded = false;
                if(data.stories){
                    $.each(data.stories, function(index, story){
                        if(story.chapters.length > 0 || self.personal == true){
                            self.stories.hash[story.id] = self.stories.array.length;
                            self.stories.array.push(story.id);
                            var storyDiv = $('<div></div>').addClass('item_wrap');
                            var storyComponent = new StoryBrief({el : storyDiv, data: story, EvAg: self.EvAg, Personal: self.personal, properties: self.properties});
                            if(story.story.image.originalUrl !== null){
                                storiesFragment.append(storyDiv);
                            } else {
                                storyDiv.imagesLoaded(function(){
                                    self.exhibit.isotope('insert', storyDiv);
                                });
                            }
                        }
                        storiesAdded = true;
                    });
                    self.exhibit.isotope('insert', storiesFragment.children(), function(){
                        self.EvAg.trigger('infiniteScroll/unlock');
                    });
                }
                return storiesAdded;
            }
        });
    }
);
