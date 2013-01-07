define(
    [
        'backbone',
        'components/utils'
    ],
    function(Backbone, utils){
        return Backbone.Model.extend({
            initialize: function(attr, options){
                this.url = options.properties.urls.api + "/api/me/settings"
            },
            saveSettings: function(){
                utils.AjaxFactory({
                    url: this.url,
                    type: 'POST',
                    processData: false,
                    contentType: "application/json",
                    data: JSON.stringify({
                        receiveNotificationEmail: this.get("receiveNotificationEmail")
                    })
                })();
            }
        })
    }
);