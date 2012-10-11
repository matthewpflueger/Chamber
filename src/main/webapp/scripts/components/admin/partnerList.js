define(
    [
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'text!templates/admin/partnerList.html',
        'text!templates/admin/partnerListRow.html'
    ],
    function($, Backbone, _, utils, templatePartnerList, templatePartnerRow){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.EvAg.bind('partnerList/show', this.render);
                this.properties = options.properties;
                this.element = $(options.el);
            },
            render: function(){
                var self = this;
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/admin/partners",
                    success: function(data){
                        var tableTemplate = _.template(templatePartnerList);
                        self.element.html(tableTemplate);
                        var body = self.element.find('tbody');
                        $.each(data, function(index, partner){
                            var rowTemplate = _.template(templatePartnerRow, partner);
                            var tr = $('<tr></tr>').html(rowTemplate);
                            body.append(tr);
                        });
                        self.element.show();
                    }
                })();

            }
        });
    }
)