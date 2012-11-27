define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'text!templates/title.html',
        'text!templates/title/topic.html'
    ],
    function($, Backbone, _, utils, templateTitle, templateTopic){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element = $(options.el);
                this.properties = options.properties;
                this.EvAg = options.EvAg;
                this.EvAg.bind('title/update', this.update);
                this.render(options);
            },
            events: {
            },
            loadTopics: function(){
                var self = this;
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/api/topics",
                    dataType: 'json',
                    success: function(response){
                        var ul = $('<ul></ul>');
                        $.each(response, function(index, topic){
                            var template = _.template(templateTopic, topic);
                            $('<li></li>').html(template).appendTo(ul);
                        });
                        ul.appendTo(self.titleBody);
                    }
                })();
            },
            render: function(options){
                this.element.html(_.template(templateTitle));
                this.titleText = $('#title-text');
                this.titleBody = $('#title-body');
                this.element.show();
            },
            update: function(options){
                this.titleText.text(options.title);
                this.titleBody.empty();
                this.loadTopics();
                this.titleBody.show();
            }
        });
    }
)
