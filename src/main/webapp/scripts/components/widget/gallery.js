define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'easyXDM'
    ],
    function($, Backbone, _, utils, easyXDM){
        return Backbone.View.extend({
            el: 'body',
            initialize: function(options){
                _.bindAll(this);
                this.properties = options.properties;
                this.element = $('body');
                this.container = $('#gallery-container');
                this.viewPort = $('#gallery-viewport');
                this.header = $('#header');
                this.socket = new easyXDM.Socket({});
                this.closeEl = $('#close');
                this.width = 220;
                this.render();
            },
            events: {
                'mouseenter': "open",
                'click #gallery-next': "next",
                'click #gallery-prev': "prev",
                'click .up': "open",
                'click .down': "close",
                "click .gallery-image-container": "loadStory",
                "click .gallery-text-container": "loadStory",
                "click #share": "share",
                "click #view": "view"
            },
            share: function(){
                this.postMessage("load", "write")
            },
            view: function(){
                this.postMessage("load", "");
            },
            loadStory: function(ev){
                var id = $(ev.currentTarget).attr('id');
                this.postMessage("load", "story/" + id);
            },
            open: function(){
                this.closeEl.removeClass('up');
                this.closeEl.addClass("down");
                this.postMessage("resize", document.body.scrollHeight);
            },
            close: function(){
                this.closeEl.removeClass('down');
                this.closeEl.addClass("up");
                this.postMessage("resize", this.header.innerHeight());
            },
            next: function(){
                if(this.container.width() > this.viewPort.width()){
                    this.container.animate({
                        left: '-=' + this.width
                    }, 'fast');
                }
            },
            prev: function(){
                var left = this.container.position().left;
                if(left < 0 && this.container.width() > this.viewPort.width()){
                    this.container.animate({
                        left: Math.min(left + this.width, 0)
                    }, 'fast');
                }
            },
            postMessage: function(type, data){
                this.socket.postMessage(JSON.stringify({
                    "type": type,
                    "data": data
                }));
            },
            render: function(){
                var self = this;
                utils.AjaxFactory({
                    url: self.properties.urls.api + "/api/partner/" + Echoed.partnerId,
                    dataType: 'json',
                    success: function(response){
                        self.container.width(0);
                        $.each(response.stories, function(index, storyFull){
                            if(storyFull.story.image || storyFull.chapterImages.length > 0){
                                var image = storyFull.story.image ? storyFull.story.image : storyFull.chapterImages[0].image;
                                var gic = $('<div></div>').addClass('gallery-image-container').attr("id", storyFull.id);
                                utils.fill(image, 75, 55).appendTo(gic);
                                gic.appendTo(self.container);
                                self.container.width(self.container.width() + gic.width() + 16);
                            } else{
                                var gic = $('<div></div>').addClass('gallery-text-container').attr("id", storyFull.id);
                                var ic = $('<div></div>').addClass('gallery-text').text(storyFull.story.title);
                                gic.append(ic).appendTo(self.container);
                                self.container.width(self.container.width() + gic.width() + 16);
                            }
                        });
                        if(self.container.width() < self.viewPort.width()){
                            self.container.css({"left" : (self.viewPort.width() / 2) - (self.container.width() / 2)});
                        }

                        console.log(self.properties);
                        if(self.properties.showGallery) self.open();
                        else self.close();
                    }
                })();
            }
        })
    }
);
