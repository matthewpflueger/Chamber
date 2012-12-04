define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'hgn!templates/admin/partnerList'
    ],
    function($, Backbone, _, utils, templatePartnerList){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.EvAg.bind('page/change', this.pageChange);
                this.properties = options.properties;
                this.element = $(options.el);
            },
            pageChange: function(option){
                if(option === "partnerList"){
                    this.render();
                } else {
                    this.hide();
                }
            },
            hide: function(){
                this.element.hide();
            },
            events: {
                "click .partnerList-button" : "clickButton"
            },
            clickButton: function(ev){
                var target = $(ev.currentTarget);
                window.location = this.properties.urls.api + "/admin/become?partnerUserId=" + target.attr('partnerUserId');
            },
            render: function(){
                var self = this;
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/admin/partners",
                    success: function(data){
                        var view = { partners : data };
                        var tableTemplate = templatePartnerList(view);
                        self.element.html(tableTemplate);
                        self.element.show();
                    }
                })();

            }
        });
    }
);