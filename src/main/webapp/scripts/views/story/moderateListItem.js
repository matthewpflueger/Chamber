define(
    [
        'marionette',
        'models/story',
        'hgn!views/story/moderateListItem'
    ],
    function(Marionette, Story, storyItemTemplate) {
        return Marionette.ItemView.extend({
            template: storyItemTemplate,
            tagName: "tr",

            events: {
                'click .moderate-cb' : 'check'
            },

            triggers: {
                'click .moderate-preview' : 'story:show'
            },

            check: function() { this.model.moderate(this.options.urls.api); }

        });
    }
);
