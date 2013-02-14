define(
    [
        'marionette',
        'models/stories',
        'models/user',
        'components/story',
        'views/story/moderateListItem',
        'hgn!views/story/moderateList'
    ],
    function(Marionette, Stories, User, Story, StoryItemView, storyListTemplate) {
        return Marionette.CompositeView.extend({
            id: "moderate",
            className: "content-container",
            template: storyListTemplate,
            itemView: StoryItemView,
            itemViewContainer: "tbody",
            collection: new Stories(),
            noFetchOnInit: false,


            collectionEvents: {
                "add": "collectionChanged",
                "remove": "collectionChanged"
            },

            initialize: function(options) {
                this.itemViewOptions = options;
                this.collection = new Stories(null, options);
                var self = this;

                if (!Marionette.getOption(this, "noFetchOnInit")) {
                    self.setMessage("Loading...");
                    this.collection.fetch({
                        error: function(collection, response, options) {
                            self.setMessage("Error: " + response);
                        },
                        success: function(collection, xhr, options) {
                            if (!collection.length) {
                                self.setMessage("No stories found!");
                            }
                        }
                    });
                }

                this.on("itemview:story:show", function(view) {
                    this.story = new Story({
                            el: '#story-container',
                            EvAg: new Marionette.EventAggregator(),
                            properties: this.options,
                            modelUser: new User({}) });
                    this.story.load(view.model.get("id"));
                });
            },

            collectionChanged: function(topic) {
                if (this.collection.length) {
                    this.setMessage("");
                } else {
                    this.setMessage("No stories!");
                }
            },

            setMessage: function(message) {
                this.$('.message').text(message);
            },

            onClose: function() {
                if (this.story) this.story.close();
            }

        });
    }
);
