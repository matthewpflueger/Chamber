define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/mobile/storyBrief',
        'components/utils',
        'text!templates/login.html',
        'isotope'
    ],
    function($, Backbone, _, StoryBrief, utils, templateLogin, isotope ){
        return Backbone.View.extend({
            el: '#content',
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.EvAg.bind('page/show', this.pageLoad);
                this.EvAg.bind('exhibit/init', this.init);
                this.EvAg.bind('infiniteScroll', this.next);
                this.EvAg.bind('exhibit/story/next', this.nextStory);
                this.EvAg.bind('exhibit/story/previous', this.previousStory);
                this.EvAg.bind('user/login', this.login);
                this.element = $(this.el);
                this.exhibit = $('#exhibit');
            },
            pageLoad: function(options){
                if(options.indexOf("exhibit") >= 0){
                    this.exhibit.fadeIn();
                } else {
                    this.exhibit.fadeOut();
                }
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
                        var titleOpt = { title: self.contentTitle, href: "" };
                        if(data.partner) titleOpt = { title: data.partner.name, href: "#write/partner/" + data.partner.id };
                        if(data.echoedUser && self.personal !== true) titleOpt = { title: data.echoedUser.name, href: "" };
                        self.EvAg.trigger("title/update", titleOpt);
                        self.EvAg.trigger("pagetitle/update", titleOpt.title);
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
                if(self.addStories(data) || self.addFriends(data) || self.addCommunities(data)){
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
            addCommunities: function(data){
                var self = this;
                var communityFragment = $('<div></div>');
                var communityAdded = false;
                if(data.communities){
                    $.each(data.communities, function(index, community){
                        var communityDiv = $('<div></div>').addClass("item_wrap");
                        $('<a class="item_content community"></a>').text(community.id).appendTo(communityDiv).attr("href", "#community/" + community.id);
                        communityFragment.append(communityDiv);
                        communityAdded = true;
                    });
                    self.exhibit.append(communityFragment.children());
                }
                return communityAdded;
            },
            addFriends: function(data){
                var self = this;
                var friendsFragment = $('<div></div>');
                var friendsAdded = false;
                if(data.friends){
                    $.each(data.friends, function(index, friend){
                        var friendImage = $('<div class="friend-img"></div>');
                        var friendText = $('<div class="friend-text"></div>').text(friend.name);
                        var  a = $('<a></a>').attr("href","#user/" + friend.toEchoedUserId).addClass('item_wrap');
                        $('<img />').attr("height","50px").attr("src",utils.getProfilePhotoUrl(friend, self.properties.urls)).appendTo(friendImage);
                        $('<div class="item_content friend"></div>').append(friendImage).append(friendText).appendTo(a).addClass('clearfix');
                        friendsFragment.append(a);
                        friendsAdded = true;
                    });
                    self.exhibit.append(friendsFragment.children());
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
                            storiesFragment.append(storyDiv)
                        }
                        storiesAdded = true;
                    });
                }
                self.exhibit.append(storiesFragment.children());
                return storiesAdded;
            }
        });
    }
);
