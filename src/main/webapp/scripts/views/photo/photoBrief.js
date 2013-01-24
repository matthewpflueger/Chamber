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
                this.modelUser =    options.modelUser;
                this.modelPhoto =   options.modelPhoto;
                this.EvAg =         options.EvAg;
                this.element =      $(options.el);
                this.render();
            },
            events: {
                "click":    "click"
            },
            render: function(){
                var content =   this.modelPhoto.toJSON();
                var img =       utils.scaleByWidth(content.image, 300);
                this.element.addClass("item-photo");
                this.element.append(img);
            },
            click: function(){
                window.location.hash = "#!photo/" +  this.modelPhoto.id;
            }
        });
    }
);