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
                this.titleActions = $('#title-actions');
                this.titleShare = $('#title-share');
                this.titleShareLabel= $('#title-share-label');
            },
            update: function(options){
                if(options.href !== ""){
                    this.titleActions.show();
                } else{
                    this.titleActions.hide();
                }
                if(options.title !== ""){
                    this.titleText.text(decodeURIComponent(options.title));
                    this.titleShareLabel.text("Share Your " + options.title + " Story");
                    this.titleShare.attr("href", options.href);
                    this.element.show()
                } else{
                    this.titleText.text("");
                    this.element.hide();
                }
            }
        });
    }
)
