define(
    [
        'jquery',
        'marionette',
        'models/customization',
        'hgn!templates/partner/customize'
    ],
    function($, Marionette, Customization, templateCustomize){
        return Marionette.ItemView.extend({
            id: "customize",
            className: "content-container",
            template: templateCustomize,

            ui: {
                form: "#partner-customize"
            },

            events: {
                "click #customize-save" : "submit"
            },

            modelEvents: {
                "change": "render"
            },

            templateHelpers: {
                isTop: function() {  if (this.remoteVertical === "top") return "checked"; else return ""; },
                isBottom: function() {  if (this.remoteVertical === "top") return ""; else return "checked"; },
                isLeft: function() { if (this.remoteHorizontal === "left") return "checked"; else return ""; },
                isRight: function() { if (this.remoteHorizontal === "left") return ""; else return "checked"; },
                isHor: function() { if (this.remoteOrientation === "hor") return "checked"; else return ""; },
                isVer: function() { if (this.remoteOrientation === "hor") return ""; else return "checked"; }
            },

            initialize: function(options) {
                this.model = new Customization({ id: options.partnerUser.partnerId });
                this.model.fetch();
            },

            submit: function() {
                var self = this;
                var data = this.ui.form.serializeArray();
                var json = {};
                $.each(data, function(index, item){
                    json[item.name] = item.value;
                });

                this.setMessage("Saving...");
                this.model.save(json, {
                    wait: true,
                    error: function(model, response, options) {
                        self.setMessage("Error: " + response);
                    },
                    success: function(model, xhr, options) {
                        self.setMessage("Saved!");
                    }
                });
            },

            setMessage: function(message) {
                this.$(".message").text(message);
            }
        });
});