define(
    'components/title',
    ['jquery', 'backbone', 'underscore'],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element = $(options.el);
                this.EvAg = options.EvAg;
                this.EvAg.bind('title/update', this.update);
                this.titleText = $('#title-text');
            },
            update: function(options){
                if(options.title !== ""){
                    this.titleText.text(decodeURIComponent(options.title));
                    this.element.show()
                } else{
                    this.titleText.text("");
                    this.element.hide();
                }
            }
        });
    }
)
