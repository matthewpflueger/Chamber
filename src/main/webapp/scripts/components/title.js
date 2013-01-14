define(
    [
        'jquery',
        'backbone',
        'underscore',
        'models/context',
        'components/utils',
        'views/follow/follow',
        'hgn!templates/title/title',
        'hgn!templates/title/topic'
    ],
    function($, Backbone, _, ModelContext, utils, Follow, templateTitle, templateTopic){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element = $(options.el);
                this.titleEl = $('#title');
                this.properties = options.properties;
                this.modelContext = options.modelContext;
                this.modelUser = options.modelUser;
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
                this.follow = new Follow({ el: "#title-follow", properties: this.properties, modelUser: this.modelUser, followId: this.modelContext.id, type: this.modelContext.get("contextType") });
                this.titleText = $('#title-text');
                this.titleBody = $('#title-body');
                this.element.show();
            },
            update: function(options){
                if(options.image) {
                    this.titleEl.css('background-image', 'url("' + utils.scaleByWidth(options.image, 260).attr('src') + '")');
                } else {
                    this.titleEl.css('background-image','');
                }
                switch(options.type){
                    case "partner":
                        this.topicEndPoint = "partner/" + options.partnerId;
                        break;
                    case "community":
                        this.topicEndPoint = "community/" + options.communityId;
                        break;
                    case "echoed":
                        this.topicEndPoint = "";
                        break;
                    default:
                        break;
                }
            }
        });
    }
)
