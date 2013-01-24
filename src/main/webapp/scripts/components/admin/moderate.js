define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'hgn!templates/admin/moderateStoryTable',
        'hgn!templates/admin/paginate'
    ],
    function($, Backbone, _, utils, templateModerateStoryTable, templatePaginate){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.properties = options.properties;
                this.element = $(options.el);
                this.EvAg.bind('page/change', this.pageChange);
                this.page = 0;
                this.pageSize = 30;
            },
            pageChange: function(option){
                console.log("TEST");
                if(option === "moderate"){
                    this.render();
                } else {
                    this.hide();
                }
            },
            render: function(){
                var self = this;
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/admin/stories?page=" + self.page + "&pageSize=" + self.pageSize,
                    success: function(data){
                        var view = { stories: data };
                        var tableTemplate = templateModerateStoryTable(view);
                        self.element.html(tableTemplate);
                        self.element.append(templatePaginate());
                        if (self.page == 0) $('#paginate-previous').hide();
                        if (data.length < self.pageSize) $('#paginate-next').hide();
                        self.show();
                    }
                })();
            },
            hide: function(){
                this.element.hide();
            },
            show: function(){
                this.element.fadeIn();
            },
            events: {
                'click .moderate-cb' : 'check',
                'click .moderate-preview' : 'preview',
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
            preview: function(ev){
                var target = $(ev.currentTarget);
                var id = target.attr('storyId');
                this.EvAg.trigger('story/show', id);
            },
            check: function(ev){
                var target = $(ev.currentTarget);
                var id = target.attr('id');
                var isModerated = target.is(':checked');
                var storyOwnerId = target.attr('storyOwnerId');
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/story/" + id + "/moderate",
                    type: "POST",
                    data: {
                        moderated: isModerated,
                        storyOwnerId : storyOwnerId
                    },
                    success: function(data){
                    }
                })();
            }
        })
    }
);