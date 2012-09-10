define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils'
    ],
    function($, Backbone, _, utils){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'show', 'keyPress');
                this.EvAg = options.EvAg;
                this.el = options.el;
                this.properties = options.properties;
                this.element = $(options.el);
                this.input = $('#ajax-input-field');
                this.list = $('#ajax-suggestions-list');
                this.suggestions = $('#ajax-input-suggestions');
                this.EvAg.bind("ajaxInput/show", this.show);
            },
            events: {
                "keyup :input": "keyPress",
                "click .suggestion-item": "click"
            },
            click: function(ev){
                var self = this;
                self.input.val($(ev.currentTarget).attr('tag'));
                self.suggestions.fadeOut();
            },
            keyPress: function(){
                var self = this;
                var filter = self.input.val();
                if(filter !== ''){
                    utils.AjaxFactory({
                        url: self.properties.urls.api+ "/api/tags/top",
                        data: {
                            tagId: filter
                        },
                        success: function(data){
                            self.render(data)
                        }
                    })();
                }
            },
            render: function(data){
                var self = this;
                self.list.empty();
                $.each(data, function(index, tag){
                    $('<div class="suggestion-item"></div>').append(tag.id + " (" + tag.counter + ")").appendTo(self.list).attr("tag", tag.id);
                });
                self.suggestions.fadeIn();
            },
            show: function(){
                var self = this;
                self.element.fadeIn();
            }
        });
    }
)

