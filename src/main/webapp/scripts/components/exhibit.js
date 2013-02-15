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
                this.EvAg.bind('exhibit/reload',    this.reload);
            },
            lookup: function(id){
                if (!this.content || this.content.hash[id] === undefined) this.EvAg.trigger('content:show', id)
                else {
                    var index = this.content.hash[id];

                    if ((index - 1) >= 0) this.nextItem(this.content.array[index - 1].id);
                    else if ((index + 1) < this.content.array.length) this.prevItem(this.content.array[index + 1].id);
                    else this.EvAg.trigger('content:show', { modelContent: this.content.array[index] });
                }
            },
            init: function(options){
                var data =      options.data;
                this.jsonUrl =  options.jsonUrl;

                if (options.personalized === true) {
                    this.personalized = true;
                } else {
                    $('#exhibit-message').hide();
                    this.personalized = false;
                }

                this.render(data);
            },
            nextItem: function(storyId){
                if (this.content.hash[storyId] === undefined) return;

                var self =  this;                
                var index = this.content.hash[storyId] + 1;
                var hasNext = (index + 1) < this.content.array.length;
                
                function showNext() {
                    if (index >= self.content.array.length) return;
                    
                    self.EvAg.trigger("content:show", { 
                        modelContent: self.content.array[index],
                        hasNext: (index + 1) < self.content.array.length,
                        hasPrevious: true 
                    });
                }

                if (!hasNext) this.more(showNext);
                else showNext();
            },
            prevItem: function(storyId){
                var index = this.content.hash[storyId] - 1;
                if (index >= 0) {
                    this.EvAg.trigger("content:show", { 
                        modelContent: this.content.array[index],
                        hasNext: true,
                        hasPrevious: (index - 1) >= 0 });
                }
            },
            render: function(data){
                if (data.nextPage) {
                    this.nextPage = data.nextPage;
                } else {
                    if (this.personalized) $('#exhibit-message').show();
                    this.nextPage = null;
                }

                this.content = {
                    array: [],
                    hash: {}
                };

                if (this.isotopeOn === true) this.exhibit.isotope("destroy");
                this.exhibit.empty();
                this.exhibit.isotope({
                    itemSelector: '.item_wrap',
                    onLayout: function(elems, instance){
                        console.log(instance);
                        if(instance.element[0].offsetWidth < instance.width){
                            $('#title-container').animate({ width: instance.element[0].offsetWidth - 12 });
                        }
                    }
                });
                this.isotopeOn = true;
                this.addContent(data);
                this.EvAg.trigger('infiniteScroll/on');
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
                            } else {
                                if (this.personalized) $('#exhibit-message').show();
                            }
                            self.addContent(data);
                            if (callback) callback();
                        }    
                    })();
                } else if (callback) callback();
            },
            reload: function(){
                var self = this;
                self.EvAg.trigger('infiniteScroll/lock');
                var url = self.jsonUrl;
                self.nextPage = null;
                utils.AjaxFactory({
                    url: url,
                    success: function(data) {
                        self.render(data);
                    }
                })();
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
