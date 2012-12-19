require(
    [
        'requireLib',
        'backbone',
        'marionette',
        'controllers/partnerController'
    ],
    function(require, Backbone, Marionette, PartnerController) {
        var PartnerDashboard = new Marionette.Application();
        PartnerDashboard.addRegions({ content: "#content" });

        var properties = {
            urls: Echoed.urls,
            partnerUser: Echoed.partnerUser,
            exhibitShowLogin: true,
            contentRegion: PartnerDashboard.content,
            vent: PartnerDashboard.vent
        };

        PartnerDashboard.addInitializer(function(options){
            new (Marionette.AppRouter.extend({
                appRoutes: {
                    "": "showModerationTab",
                    "topics": "showTopicTab",
                    "customize": "showCustomizationTab",
                    "install": "showHowToInstallTab"
                },
                controller: new PartnerController(options)
            }))();
        });
        PartnerDashboard.on("initialize:after", function(options) { Backbone.history.start(); });
        PartnerDashboard.start(properties);
    }
);


