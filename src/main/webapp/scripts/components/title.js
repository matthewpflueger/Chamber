define(
    [
        'jquery',
        'backbone',
        'underscore',
        'models/context',
        'components/utils',
        'hgn!templates/title/title',
        'hgn!templates/title/topic'
    ],
    function($, Backbone, _, ModelContext, utils, templateTitle, templateTopic){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element = $(options.el);
                this.titleEl = $('#title');
                this.properties = options.properties;
                this.modelContext = options.modelContext;
                this.modelContext.on("change", this.render);
                this.EvAg = options.EvAg;
//                this.render(options);
            },
            events: {
            },
            loadTopics: function(){
                var self = this;
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/api/topics/" + this.topicEndPoint,
                    dataType: 'json',
                    success: function(response){
                        if (response.length == 0) {
                            self.titleBody.hide();
                            return;
                        }
                        
                        var ul = $('<ul></ul>');
                        $.each(response, function(index, topic){
                            var view = { topic: topic };
                            var template = templateTopic(view);
                            $(template).appendTo(ul);
                        });
                        self.titleBody.append($('<div id="title-body-title">Suggested Topics</div>'));
                        ul.appendTo(self.titleBody);
                        self.titleBody.show();
                    }
                })();
            },
            render: function(){
                this.element.html(templateTitle({context: this.modelContext.toJSON()}));
                this.titleText = $('#title-text');
                this.titleBody = $('#title-body');
                this.element.show();
            },
            update: function(options){
//                this.titleText.text(decodeURIComponent(options.title));
//              this.titleBody.empty();
                if(options.image) {
                    this.titleEl.css('background-image', 'url("' + utils.scaleByWidth(options.image, 260).attr('src') + '")');
                } else {
                    this.titleEl.css('background-image','');
                }
                switch(options.type){
                    case "partner":
                        this.topicEndPoint = "partner/" + options.partnerId;
//                        this.loadTopics();
                        break;
                    case "community":
                        this.topicEndPoint = "community/" + options.communityId;
//                        this.loadTopics();
                        break;
                    case "echoed":
                        this.topicEndPoint = "";
//                        this.loadTopics();

                        break;
                    default:
//                        this.titleBody.hide();
                        break;
                }
            }
        });
    }
)
