define(
    [
        'marionette',
        'views/howToInstall',
        'views/story/moderateList',
        'views/customize',
        'views/topic/topicTabLayout'
    ],
    function (Marionette, HowToInstall, Moderate, Customize, TopicTabLayout) {
        return Marionette.Controller.extend({
            initialize: function(options) {
                this.contentRegion = options.contentRegion
            },
            showTopicTab: function() {
                this.contentRegion.show(new TopicTabLayout());
            },
            showCustomizationTab: function() {
                this.contentRegion.show(new Customize(this.options));
            },
            showModerationTab: function() {
                this.contentRegion.show(new Moderate(this.options));
            },
            showHowToInstallTab: function() {
                this.contentRegion.show(new HowToInstall(this.options));
            }
        });
    }
);