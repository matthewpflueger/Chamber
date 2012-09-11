define(
    'components/title',
    ['jquery', 'backbone', 'underscore'],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'update');
                this.element = $(options.el);
                this.EvAg = options.EvAg;
                this.EvAg.bind('title/update', this.update);
                this.titleText = $('#title-text');
            },
            update: function(options){
                if(options.title !== undefined){
                    this.titleText.html(decodeURIComponent(options.title));
                    this.element.show()
                } else{
                    this.titleText.html("");
                    this.element.hide();
                }
            }
        });
    }
)
