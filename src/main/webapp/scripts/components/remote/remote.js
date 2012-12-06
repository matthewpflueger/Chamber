define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'hgn!templates/remote/remote',
        'hgn!templates/remote/stories'
    ],
    function($, Backbone, _, utils, templateRemote, templateStories){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.el = options.el;
                this.properties = options.properties;
                this.element = $(this.el);
                var self = this;
                this.render();
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/api/partner/" + this.properties.partnerId,
                    dataType: 'json',
                    success: function(response){
                        self.stories = response.stories;
                        self.storyIndex = 0;
                        var i = 0, counter = 0;
                        while(i < self.stories.length && counter < 4){
                            var story = self.stories[i];
                            if(story.story.image) {
                                var link = $('<a></a>').attr("href", "#echoed_story/" + story.id).append(utils.fit(story.story.image, 40 , 40)).attr('index', i).addClass("echoed-story");
                                $('#echoed-options').prepend(link);
                                counter++;
                            }
                            i++;
                        }
                    }
                })();
            },
            events: {
                "click .echoed-story" : "",
                "mouseenter #echoed-add": "",
                "mouseleave #echoed-add": ""
            },
            render: function(){
                var template = templateRemote();
                this.element.html(template);
                this.options = $('#echoed-options');
            }
        });
    });