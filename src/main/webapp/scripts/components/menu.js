define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'text!templates/menu.html'
    ],
    function($, Backbone, _, utils, templateMenu){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'load', 'unload', 'navigate');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.el = options.el;
                this.element = $(this.el);
                this.EvAg.bind('menu/show', this.load);
                this.render();
            },
            events: {
                "click .menu-close": "unload",
                "click .menu-item": "navigate"
            },
            load: function(){
                var self = this;
                self.template = _.template(templateMenu);
                self.element.html(self.template);
                self.content = $('#menu-content');
                utils.AjaxFactory({
                    url: self.properties.urls.api + "/api/tags",
                    success: function(data){
                        $.each(data, function(index, tag){
                            var colStr = (Math.floor(index / (data.length / 3)) +1).toString();
                            $('#menu-column-' + colStr).append($('<div></div>').addClass('menu-item').append(tag.id + " (" + tag.counter + ")").attr("href", encodeURIComponent(tag.id)));
                        });
                        self.EvAg.trigger('fade/show');
                        self.element.show();
                    }
                })();
            },
            navigate: function(ev){
                var self = this;
                var href = $(ev.currentTarget).attr("href");
                self.EvAg.trigger('fade/hide');
                window.location.hash = "#!category/" + href;

            },
            unload: function(){
                var self = this;
                self.element.fadeOut();
                self.element.empty();
                self.EvAg.trigger('fade/hide');
            }
        });


    }
);