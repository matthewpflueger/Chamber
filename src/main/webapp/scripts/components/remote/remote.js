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
                _.bindAll(this);
                this.el = options.el;
                this.element = $(this.el);
                this.render();
            },
            render: function(){
                this.element.html('<div id="echoed-icon"></div><div id="echoed-options-container"><div id="echoed-options" class="echoed-options"></div></div>');
            }
        });
    });