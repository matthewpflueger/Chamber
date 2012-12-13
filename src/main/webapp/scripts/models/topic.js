define(
    [
        'backbone'
    ],
    function(Backbone) {
        return Backbone.Model.extend({
            urlRoot: "settings/topics",

            defaults: {
                isEditable: true
            },

            validate: function(attr) {
                if (!this.get("isEditable")) return "not editable";
                if (attr.endOn && attr.beginOn > attr.endOn) {
                    return "end date is before begin date";
                }
            }
        });
    }
);