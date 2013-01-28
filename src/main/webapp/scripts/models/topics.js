define(
    [
        'backbone',
        'models/topic'
    ],
    function(Backbone, Topic) {
        return Backbone.Collection.extend({
            url: "/api/partner/topics",
            model: Topic
        });
    }
);