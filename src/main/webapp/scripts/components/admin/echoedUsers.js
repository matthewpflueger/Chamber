define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'hgn!templates/admin/echoedUsers',
        'hgn!templates/admin/paginate'
    ],
    function($, Backbone, _, utils, templateEchoedUsers, templatePaginate){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.EvAg.bind('page/change', this.pageChange);
                this.properties = options.properties;
                this.element = $(options.el);
                this.page = 0;
                this.pageSize = 30;
            },
            pageChange: function(option){
                if(option === "echoedUsers"){
                    this.render();
                } else {
                    this.hide();
                }
            },
            events: {
                'click .paginate-next' : 'paginateNext',
                'click .paginate-previous' : 'paginatePrevious'
            },
            paginateNext: function() {
                this.page = this.page + 1;
                this.render();
            },
            paginatePrevious: function() {
                this.page = this.page - 1;
                this.render();
            },
            hide: function(){
                this.element.hide();
            },
            render: function(){
                var self = this;
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/admin/echoedusers?page=" + self.page + "&pageSize=" + self.pageSize,
                    success: function(data){
                        var view = { echoedUsers: data };
                        var template = templateEchoedUsers(view);
                        self.element.html(template);
                        self.element.append(templatePaginate());
                        if (self.page == 0) $('.paginate-previous').hide();
                        if (data.length < self.pageSize) $('.paginate-next').hide();
                        self.element.show();
                    }
                })();

            }
        });
    }
)