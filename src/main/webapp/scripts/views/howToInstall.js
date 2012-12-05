define(
    [
        'backbone',
        'marionette',
        'hgn!templates/partner/howToInstall'
    ],
    function(Backbone, Marionette, templatesHowToInstall){
        return Marionette.ItemView.extend({
            template: templatesHowToInstall,
            id: "howToInstall",
            className: "content-container",

            initialize: function(options) {
                this.model = new Backbone.Model(options);
            }
        });
    }
)

