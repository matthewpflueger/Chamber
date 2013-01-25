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
                this.EvAg.bind('infiniteScroll',    this.more);
                this.EvAg.bind('exhibit:next',      this.nextItem);
                this.EvAg.bind('exhibit:previous',  this.prevItem);
                this.EvAg.bind('content:lookup',    this.lookup);

            },
            lookup: function(id){
                var lookup = id;
                if(this.content){
                    if(this.content.hash[id] !== undefined) lookup = { modelContent: this.content.array[this.content.hash[id]] };
                }
                this.EvAg.trigger('content:show', lookup);
            },
            init: function(options){
                var data =      options.data;
                this.jsonUrl =  options.jsonUrl;

                this.nextPage = data.nextPage ? data.nextPage : null;
                this.content = {
                    array: [],
                    hash: {}
                };

                if (this.isotopeOn === true) this.exhibit.isotope("destroy");
                this.exhibit.empty();
                this.exhibit.isotope({
                    itemSelector: '.item_wrap',
                    onLayout: function(elems, instance){
                    }
                });
                this.isotopeOn = true;
                this.render(data);
                this.EvAg.trigger('infiniteScroll/on');

            },
            nextItem: function(storyId){
                var self =  this;
                var index = this.content.hash[storyId];
                if((index + 1) >= this.content.array.length){
                    this.more(function(){
                        if((index + 1) < self.content.array.length){
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
                this.addContent(data);
            },
            more: function(callback){
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
                            self.addContent(data);
                            if(callback) callback();
                        }
                    })();
                }
            },
            addContent: function(data){
                var self = this;
                var contentFragment = $('<div></div>');
                var contentAdded = false;
                $.each(data.content, function(index, content){
                    switch( content.contentType ){
                        case "Story":
                            var storyDiv =                  $('<div></div>').addClass('item_wrap');
                            var modelStory =                new ModelStory(content, { properties: self.properties});
                            var storyComponent =            new StoryBrief({
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
                        case "Photo":
                            var photoDiv =                      $('<div></div>').addClass('item_wrap');
                            var modelPhoto =                    new ModelPhoto(content, { properties: self.properties });
                            var photoView =                     new PhotoBrief({
                                                                    el:             photoDiv,
                                                                    modelPhoto:     modelPhoto,
                                                                    modelUser:      self.modelUser,
                                                                    properties:     self.properties,
                                                                    EvAg:           self.EvAg
                                                                });
                            self.content.hash[content.id] =    self.content.array.length;

                            self.content.array.push(modelPhoto);
                            contentFragment.append(photoDiv);
                            break;
                        case "Partner":
                            var friendText =                    $('<div class="friend-text"></div>').text(content.name);
                            var a =                             $('<a></a>').attr("href", "#partner/" + content.partnerId).addClass("item_wrap");
                            $('<div class="item_content friend"></div>').append(friendText).appendTo(a);
                            contentFragment.append(a);
                            break;
                        case "User":
                            var friendImage =                   $('<div class="friend-img"></div>');
                            var friendText =                    $('<div class="friend-text"></div>').text(content.name);
                            var  a =                            $('<a></a>').attr("href","#user/" + content.echoedUserId).addClass('item_wrap');
                            $('<img />').attr("height","50px")
                                .attr("src",utils.getProfilePhotoUrl(content, self.properties.urls))
                                .appendTo(friendImage);
                            $('<div class="item_content friend"></div>')
                                .append(friendImage)
                                .append(friendText)
                                .appendTo(a)
                                .addClass('clearfix');
                            contentFragment.append(a);
                            break;
                    }
                    contentAdded = true;
                });
                self.exhibit.isotope('insert', contentFragment.children(), function(){
                    self.EvAg.trigger('infiniteScroll/unlock');
                });
                return contentAdded;
            }
        });
    }
);
