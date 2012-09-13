define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'click');
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.element = $(this.el);
                this.element.show();
                this.render();
            },
            events: {
                'click #action-share': 'click'
            },
            render: function(){
                this.element.empty();
                this.element.append($("<div class='action-button' id='action-share'>Share Your Story</div>"));
            },
            click: function(e){
                window.location.hash = "#write/";
            }
        });
    }
);