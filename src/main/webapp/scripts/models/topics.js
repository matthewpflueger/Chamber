define(
    [
        'backbone',
        'models/topic'
    ],
    function(Backbone, Topic) {
        return Backbone.Collection.extend({
            url: "settings/topics",
            model: Topic
        });
    }
);