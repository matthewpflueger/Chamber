define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'hgn!templates/partner/customize'
    ],
    function($, Backbone, _, utils, templateCustomize){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.element = $(this.el);
                this.render();
            },
            events: {
                "click #customize-save" : "submit"
            },
            render: function(){
                var self = this;
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/partner/settings/customization",
                    success: function(customization){
                        if(customization.remoteVertical === "top") customization.isTop = true;
                        if(customization.remoteHorizontal === "left") customization.isLeft = true;
                        if(customization.remoteOrientation === "hor") customzation.isHor = true;
                        var template = templateCustomize(customization);
                        self.element.html(template);
                        self.form = $('#partner-customize');
                    }
                })();
            },
            submit: function(){
                var data = this.form.serializeArray();
                var json = {};
                $.each(data, function(index, item){
                    json[item.name] = item.value;
                });
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/partner/settings/customization",
                    type: "PUT",
                    processData: false,
                    contentType: "application/json",
                    data: JSON.stringify(json),
                    success: function(chapterSubmitResponse) {
                        alert("Customized Settings Saved")
                    }
                })();
            }
        })
});