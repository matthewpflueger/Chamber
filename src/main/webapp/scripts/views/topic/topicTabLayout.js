define(
    [
        'marionette',
        'models/topics',
        'views/topic/topicListItem',
        'views/topic/topicList',
        'hgn!views/topic/topicTabLayout'
    ],
    function(Marionette, Topics, TopicItemView, TopicListView, topicTabLayoutTemplate) {
        return Marionette.Layout.extend({
            template: topicTabLayoutTemplate,
            id: "topics",
            className: "content-container",
            regions: {
                topicCreateRegion: "#topic-create tbody",
                topicListRegion: "#topic-list"
            },
            topics: new Topics(),

            initialize: function(options) {
                this.topics = Marionette.getOption(this, "topics");
            },

            onRender: function() {
                this.topicCreateRegion.show(new TopicItemView({ isCreateOnly: true, collection: this.topics }));
                this.topicListRegion.show(new TopicListView({ collection: this.topics }));
            }
        });
    }
);