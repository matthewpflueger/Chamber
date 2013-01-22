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
                this.modelPhoto =   options.modelPhoto;
                this.render();
            },
            render: function(){
                var tmp = templatePhoto();
                this.element.html(tmp);
                var img = utils.fit(this.modelPhoto.get("image"), 866, 700);
                this.element.addClass("item-photo");
                this.element.append(img);
            }
        });
    }
);