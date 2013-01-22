define(
    [
        'jquery',
        'backbone',
        'underscore',
        'models/story',
        'models/photo',
        'components/storyBrief',
        'views/photo/photoBrief',
        'components/utils',
        'isotope'
    ],
    function($, Backbone, _, ModelStory, ModelPhoto, StoryBrief, PhotoBrief, utils, isotope ){
        return Backbone.View.extend({
            el: '#content',
            initialize: function(options){
                _.bindAll(this);
                this.EvAg =         options.EvAg;
                this.properties =   options.properties;
                this.modelUser =    options.modelUser;
                this.element =      $(this.el);
                this.exhibit =      $('#exhibit');

                this.EvAg.bind('exhibit/init',      this.init);
                this.EvAg.bind('infiniteScroll',    this.nextPage);
                this.EvAg.bind('exhibit:next',      this.nextItem);
                this.EvAg.bind('exhibit:previous',  this.prevItem);

            },
            init: function(options){
                var self =      this;
                var data =      options.data;
                self.jsonUrl =  options.jsonUrl;

                self.nextPage = data.nextPage ? data.nextPage : null;
                self.content = {
                    array: [],
                    hash: {}
                };
                if (self.isotopeOn === true) {
                    self.exhibit.isotope("destroy")
                }
                self.exhibit.empty();
                self.exhibit.isotope({
                    itemSelector: '.item_wrap'
                });
                self.isotopeOn = true;
                self.render(data);
                self.EvAg.trigger('infiniteScroll/on');
            },
            nextItem: function(storyId){
                var self = this;
                var index = this.content.hash[storyId];
                if((index + 1) >= this.content.array.length){
                    this.next(function(){
                        if((index + 1) < this.content.array.length){
                            self.EvAg.trigger("content:show", { modelContent: self.content.array[index + 1] });
                        }
                    });
                } else {
                    if((index + 1) < this.content.array.length){
                        self.EvAg.trigger("content:show", { modelContent: self.content.array[index + 1] });
                    }
                }

            },
            prevItem: function(storyId){
                var index = this.content.hash[storyId];
                if(index > 0){
                    this.EvAg.trigger("content:show", { modelContent: this.content.array[index - 1] });
                }
            },
            render: function(data){
                if(this.addContent(data) || this.addFriends(data) || this.addCommunities(data)){
                    this.EvAg.trigger('infiniteScroll/unlock');
                }
            },
            next: function(callback){
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
                            if(callback) callback();
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
                    self.exhibit.isotope('insert', communityFragment.children());
                }
                return communityAdded;
            },
            addFriends: function(data){
                var self = this;
                var friendsFragment = $('<div></div>');
                var friendsAdded = false;
                if(data && data.length > 0){
                    $.each(data, function(index, friend){
                        var friendImage = $('<div class="friend-img"></div>');
                        var friendText = $('<div class="friend-text"></div>').text(friend.name);
                        var  a = $('<a></a>').attr("href","#user/" + friend.echoedUserId).addClass('item_wrap');
                        $('<img />').attr("height","50px").attr("src",utils.getProfilePhotoUrl(friend, self.properties.urls)).appendTo(friendImage);
                        $('<div class="item_content friend"></div>').append(friendImage).append(friendText).appendTo(a).addClass('clearfix');
                        friendsFragment.append(a);
                        friendsAdded = true;
                    });
                    self.exhibit.isotope('insert', friendsFragment.children());
                }
            },
            addContent: function(data){
                var self = this;
                var contentFragment = $('<div></div>');
                var contentAdded = false;
                if(data.content){
                    $.each(data.content, function(index, content){
                        switch( content._type ){
                            case "story":
                                var storyDiv = $('<div></div>').addClass('item_wrap');
                                var modelStory = new ModelStory(content, { properties: self.properties});
                                var storyComponent = new StoryBrief({
                                    el:         storyDiv,
                                    data:       content,
                                    EvAg:       self.EvAg,
                                    Personal:   self.personal,
                                    properties: self.properties,
                                    modelUser:  self.modelUser,
                                    modelStory: modelStory
                                });
                                self.content.hash[content.id] = self.content.array.length;
                                self.content.array.push(modelStory);
                                contentFragment.append(storyDiv);
                                break;
                            case "photo":
                                var photoDiv = $('<div></div>').addClass('item_wrap');
                                var modelPhoto = new ModelPhoto(content, { properties: self.properties });
                                var photoView = new PhotoBrief({
                                    el:             photoDiv,
                                    modelPhoto:     modelPhoto,
                                    modelUser:      self.modelUser,
                                    properties:     self.properties,
                                    EvAg:           self.EvAg
                                });
                                self.content.hash[content._id] = self.content.array.length;
                                self.content.array.push(modelPhoto);
                                contentFragment.append(photoDiv);
                                break;
                            case "user":
                                var friendDiv = $('<div></div>').addClass("item_wrap");
                        }
                    });
                    self.exhibit.isotope('insert', contentFragment.children(), function(){
                        self.EvAg.trigger('infiniteScroll/unlock');
                    });
                }
                return contentAdded;
            }
        });
    }
);
