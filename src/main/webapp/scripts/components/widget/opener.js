define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.render();
            },
            render: function(){
                if(this.properties.hideOpener){
                    $('#echoed-opener').hide();
                } else if(this.properties.useOpener ){
                    var open = $('#echoed-opener');
                    if(open.length === 0){
                           $('<div id="echoed-opener"></div>').append($('<img style="display: block;"/>').attr("src", this.properties.urls.images+ "/bk_opener_dark_left.png")).appendTo($('body'));
                    }
                }
            },
            events: {
                "click .echoed-opener": "show",
                "click #echoed-opener": "show"
            },
            show: function(){
                this.EvAg.trigger('overlay/show');
            }
        })
    }
);
