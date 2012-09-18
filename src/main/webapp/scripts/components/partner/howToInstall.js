define(
    [
        'jquery',
        'backbone',
        'underscore',
        'text!templates/partner/howToInstall.html'
    ],
    function($, Backbone, _, templatesHowToInstall){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'show', 'hide');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.element = $(options.el);

                this.EvAg.bind('install/show', this.show);
                this.EvAg.bind('install/hide', this.hide);

                this.render();
            },
            render: function(){
                var template = _.template(templatesHowToInstall, this.properties);
                this.element.html(template);
            },
            show: function(){
                this.element.fadeIn();
            },
            hide: function(){
                this.element.hide();
            }
        })
    }
)
