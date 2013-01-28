define(
    [
        'backbone'
    ],
    function(Backbone) {
        return Backbone.Model.extend({
            urlRoot: "/api/partner/topics",

            validate: function(attr) {
                if (attr.endOn && attr.beginOn > attr.endOn) {
                    return "end date is before begin date";
                }
            }
        });
    }
);