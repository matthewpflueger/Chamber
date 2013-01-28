define(
    [
        'backbone',
        'models/notification',
        'components/utils'
    ],
    function(Backbone, ModelNotification, utils){
        return Backbone.Collection.extend({
            model: ModelNotification,
            initialize: function(attr, options){
                this.url = options.properties.urls.site + "/api/me/notifications"
            },
            markAsRead: function(){
                var ids = this.pluck("id");
                if(ids.length){
                    utils.AjaxFactory({
                        url: this.url,
                        type: "POST",
                        data: {
                            'ids': ids
                        },
                        traditional: true
                    })();
                }
            }
        });
    }
);