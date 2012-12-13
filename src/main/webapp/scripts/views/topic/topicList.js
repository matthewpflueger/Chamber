define(
    [
        'marionette',
        'models/topics',
        'views/topic/topicListItem',
        'hgn!views/topic/topicList'
    ],
    function(Marionette, Topics, TopicItemView, topicListTemplate) {
        return Marionette.CompositeView.extend({
            tagName: "table",
            id: "moderate-table",
            className: "",
            template: topicListTemplate,
            itemView: TopicItemView,
            itemViewContainer: "tbody",
            collection: new Topics(),
            noFetchOnInit: false,


            collectionEvents: {
                "add": "collectionChanged",
                "remove": "collectionChanged"
            },

            initialize: function(options) {
                var self = this;

                if (!Marionette.getOption(this, "noFetchOnInit")) {
                    self.setMessage("Loading...");
                    this.collection.fetch({
                        error: function(collection, response, options) {
                            self.setMessage("Error: " + response);
                        },
                        success: function(collection, xhr, options) {
                            if (!collection.length) {
                                self.setMessage("No topics found!");
                            }
                        }
                    });
                }
            },

            collectionChanged: function(topic) {
                if (this.collection.length) {
                    this.setMessage("");
                } else {
                    this.setMessage("No topics!");
                }
            },

            setMessage: function(message) {
                this.$('.message').text(message);
            }

        });
    }
);