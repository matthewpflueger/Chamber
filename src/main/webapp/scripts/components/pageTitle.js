define(
    'components/pageTitle',
    ['jquery', 'backbone', 'underscore'],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'update');
                this.element = $(options.el);
                this.EvAg = options.EvAg;
                this.EvAg.bind('pagetitle/update', this.update);
            },
            update: function(text){
                this.element.html("Echoed | " + text);
            }
        });
    }
)
