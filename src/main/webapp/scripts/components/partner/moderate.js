define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'text!templates/partner/moderateStory.html',
        'text!templates/partner/moderateStoryTable.html'
    ],
    function($, Backbone, _, utils, templateModerateStory, templateModerateStoryTable){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.element = $(options.el);
                this.EvAg.bind('moderate/show', this.render);
                this.EvAg.bind('moderate/hide', this.hide);
            },
            render: function(){
                var self = this;
                var tableTemplate = _.template(templateModerateStoryTable);
                self.element.html(tableTemplate);
                var body = self.element.find('tbody');

                utils.AjaxFactory({
                    url: this.properties.urls.api + "/partner/stories",
                    success: function(data){
                        $.each(data, function(index, story){
                            var template = _.template(templateModerateStory, story);
                            var tr = $('<tr></tr>').html(template).appendTo(body);
                            if(story.isModerated){
                                tr.find('.moderate-cb').attr("checked","checked");
                            }
                        });
                        self.show();
                    }
                })();
            },
            hide: function(){
                this.element.hide();
            },
            show: function(){
                this.element.fadeIn();
            },
            events: {
                'click .moderate-cb' : 'check',
                'click .moderate-preview' : 'preview'
            },
            preview: function(ev){
                var target = $(ev.currentTarget);
                var id = target.attr('storyId');
                this.EvAg.trigger('story/show', id);
            },
            check: function(ev){
                var target = $(ev.currentTarget);
                var id = target.attr('id');
                var isModerated = target.is(':checked');
                var storyOwnerId = target.attr('storyOwnerId');
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/story/" + id + "/moderate",
                    data: {
                        moderated: isModerated,
                        storyOwnerId : storyOwnerId
                    },
                    success: function(data){
                    }
                })();
            }
        })
    }
);