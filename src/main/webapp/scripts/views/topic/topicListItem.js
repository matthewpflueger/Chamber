define(
    [
        'jquery',
        'marionette',
        'jqueryUIFull',
        'components/utils',
        'models/topic',
        'hgn!views/topic/topicListItem',
        'hgn!views/topic/topicListItemInput'
    ],
    function($, Marionette, jqueryUI, utils, Topic, topicItemTemplate, topicInputTemplate) {
        return Marionette.ItemView.extend({
            template: topicItemTemplate,
            tagName: "tr",
            className: "topic_item",
            dateFormat: "mm/dd/yy",

            events: {
                "click .edit": "edit",
                "click .cancel": "cancel",
                "click .save": "save"
            },

            ui: {
                title: ".title",
                beginOn: ".beginOn",
                endOn: ".endOn"
            },

            initialize: function(options) {
                if (options.isCreateOnly) this.isEditing = this.isCreateOnly = true;
                if (!this.model) this.model = new Topic();
                this.vent = options.vent;
            },

            getTemplate: function() {
                if (this.isEditing) return topicInputTemplate;
                return topicItemTemplate;
            },

            templateHelpers: function() {
                var self = this;
                var makeDate = function(dateLong) {
                    if (dateLong) {
                        return $.datepicker.formatDate(self.dateFormat, utils.timeStampStringToDate("" + dateLong));
                    } else {
                        return null;
                    }
                };

                return {
                    formattedBeginOn: function() { return makeDate(this.beginOn); },
                    formattedEndOn: function() { return makeDate(this.endOn); }
                };
            },

            onRender: function() {
                var self = this;
                if (!this.isEditing) return;
                this.$(".beginOn" ).datepicker({
                    minDate: 0,
                    onClose: function(selectedDate) {
                        self.$('.endOn').datepicker("option", "minDate", selectedDate);
                    }
                });
                this.$(".endOn" ).datepicker({
                    minDate: "+1d"
                });
            },

            save: function() {
                var self = this;
                var attrs = {
                    "title": this.ui.title.val(),
                    "beginOn": this.ui.beginOn.datepicker("getDate"),
                    "endOn" : this.ui.endOn.datepicker("getDate")
                };


                this.model.save(
                    attrs,
                    {
                        wait: true,
                        error: function(model, error) {
                            alert("Error: " + error);
                        },
                        success: function(model, response) {
                            if (self.isCreateOnly) {
                                if (self.collection) self.collection.add(self.model);
                                self.model = new Topic();
                            } else {
                                self.isEditing = false;
                            }
                            self.render();
                        }
                    });
            },

            edit: function() {
                this.isEditing = true;
                this.render();
            },

            cancel: function() {
                if (this.isCreateOnly) {
                    this.model = new Topic();
                } else {
                    this.isEditing = false;
                }
                this.render();
            }
        });
    }
);