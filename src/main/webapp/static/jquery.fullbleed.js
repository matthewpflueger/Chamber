var WindowSize = (function () {
    return {
        width : function () { return window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth); },
        height : function () { return window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight); }
    };
})();

FullBleed = Backbone.View.extend({
    initialize: function() {
        this.el = $(window);
        this.bgImage = null;
        this.resizeTimer = null;
        this.bgImage = $('#mainImage');
        if (this.bgImage) {
            this.scale();
            this.bgImage.css('height','');
            this.bgImage.css('width','');
            this.bgImage.css("display","block");
        }
        var self = this;
        $(window).resize(function(){
            self.scale();
        });
        document.body.style.visibility = 'visible';
        self.scale();
    },
    ieScale: function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(scale, 100);
    },
    scale: function () {
        var imgwidth = this.bgImage.width(),
            imgheight = this.bgImage.height(),
            winwidth = WindowSize.width(),
            winheight = WindowSize.height(),
            widthratio = winwidth / imgwidth,
            heightratio = winheight / imgheight,
            widthdiff = heightratio * imgwidth,
            heightdiff = widthratio * imgheight,
            newWidth,
            newHeight;

        if (heightdiff > winheight) {
            newWidth = winwidth;
            newHeight = heightdiff;
        } else {
            newWidth = widthdiff;
            newHeight = winheight;
        }
        this.bgImage.css("height",newHeight + 'px');
    }
});
