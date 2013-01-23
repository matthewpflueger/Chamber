define(
    [
        'jquery',
        'backbone',
        'underscore',
        'hgn!views/photo/photo',
        'components/utils'
    ],
    function($, Backbone, _, templatePhoto, utils){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);

                this.modelUser =    options.modelUser;
                this.EvAg =         options.EvAg;
                this.element =      $(options.el);
            },
            load: function(options){
                this.modelPhoto =   options.modelPhoto;
                this.render();
            },
            render: function(){
                var tmp = templatePhoto(this.modelPhoto.toJSON());
                var img = utils.scaleByWidth(this.modelPhoto.get("image"), 866);

                this.element.html(tmp).addClass("item-photo");
                $('#photo-image-container').append(img);
            }
        });
    }
);