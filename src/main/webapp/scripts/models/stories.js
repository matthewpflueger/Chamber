define(
    [
        'backbone',
        'models/story'
    ],
    function(Backbone, Story) {
        return Backbone.Collection.extend({
            url: "/api/partner/stories",
            model: Story
        });
    }
);