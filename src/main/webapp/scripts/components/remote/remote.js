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
                this.element.append($('<div id="ech-icon-container"><div id="echoed-icon"></div></div>'));
                this.element.append($('<div id="echoed-options" class="echoed-options"></div>'));
            }
        });
    });