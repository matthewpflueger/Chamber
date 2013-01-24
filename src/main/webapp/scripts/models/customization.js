define(
    [
        'backbone'
    ],
    function(Backbone) {
        return Backbone.Model.extend({
            url: "/api/partner/settings/customization/"
        });
    }
);