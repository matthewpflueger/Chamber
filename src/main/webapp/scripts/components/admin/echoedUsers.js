define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'text!templates/admin/echoedUsers.html',
        'text!templates/admin/echoedUserRow.html',
        'text!templates/admin/paginate.html'
    ],
    function($, Backbone, _, utils, templatesEchoedUsers, templatesEchoedUserRow, templatePaginate){
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
                        var tableTemplate = _.template(templatesEchoedUsers);
                        self.element.html(tableTemplate);
                        var body = self.element.find('tbody');
                        $.each(data, function(index, echoedUser){
                            var rowTemplate = _.template(templatesEchoedUserRow, echoedUser);
                            var tr = $('<tr></tr>').html(rowTemplate);
                            body.append(tr);
                        });


                        self.element.append(_.template(templatePaginate));
                        if (self.page == 0) $('.paginate-previous').hide();
                        if (data.length < self.pageSize) $('.paginate-next').hide();

                        self.element.show();
                    }
                })();

            }
        });
    }
)