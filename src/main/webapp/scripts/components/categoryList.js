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
                _.bindAll(this, 'seeMore', 'render');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.el = options.el;
                this.element = $(this.el);
                this.list = $('#category-list');
                this.EvAg.bind("category/refresh", this.render);
                this.render()
            },
            events: {
                "click #category-more" : "seeMore",
                "click .category-menu-item" : "navigate"

            },
            render: function(){
                var self = this;
                self.list.empty();
                utils.AjaxFactory({
                    url: self.properties.urls.api + "/api/tags/top",
                    success: function(data){
                        $.each(data, function(index, tag){
                            var categoryItem = $('<div class="category-menu-item"></div>').text(tag.id + " (" + tag.counter + ")").attr("href", tag.id);
//                            var categoryItem = $('<div class="category-menu-item"></div>').html(tag.id + " (" + tag.counter + ")").attr("href", tag.id);
                            self.list.append(categoryItem);
                        });
                    }
                })();
            },
            navigate: function(ev){
                var target = $(ev.currentTarget);
                window.location.hash = "!category/" + target.attr("href");
            },
            seeMore: function(){
                this.EvAg.trigger("menu/show")
            }
        });
    }
)
