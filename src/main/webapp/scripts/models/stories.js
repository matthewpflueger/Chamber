define(
    [
        'backbone',
        'models/story'
    ],
    function(Backbone, Story) {
        return Backbone.Collection.extend({
            url: "stories",
            model: Story
        });
    }
);