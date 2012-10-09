define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this,'render','click','open','selectOption','close');
                this.el = options.el;
                this.element = $(options.el);
                this.optionsArray = options.optionsArray;
                this.currentTitle = options.currentTitle;
                this.openState = false;
                this.defaultTopic = "(Write Your Own Topic)";
                this.render();
            },
            events: {
                "click .field-question-label" : "click",
                "click .field-question-option" : "selectOption",
                "keyup :input": "keyPress"
            },
            val: function(){
                return this.input.val();
            },
            render: function(){
                var self = this;
                self.input = $("<input type='text' class='field-text-input'>");

                self.label = $("<div class='field-question-label'></div>");
                self.downArrow = $("<div class='downarrow'></div>");
                self.label.append(self.downArrow);
                self.label.append(self.input);
                self.element.append(self.label);
                self.optionsList = $("<div class='field-question-options-list'></div>").css("display", "none");
                self.element.append(self.optionsList);
                $.each(self.optionsArray, function(index, option){
                    self.options[index] = $("<div class='field-question-option'></div>").append(option);
                    self.optionsList.append(self.options[index]);
                });
                self.options[self.optionsArray.length] = $("<div class='field-question-option'></div>").append(self.defaultTopic);
                self.optionsList.append(self.options[self.optionsArray.length]);
                self.input.val(self.options[0].text());
            },
            keyPress: function(e){
                switch(e.keyCode){
                    case 13:
                        this.close();
                }
            },
            click: function(){
                var self = this;
                if(self.locked !== true){
                    if(self.openState == false ){
                        self.open();
                    } else {
                        self.openState = false;
                        self.close();
                    }
                }
            },
            selectOption: function(e){
                var target = $(e.target);
                this.input.val(target.text());
                this.close();
            },
            close: function(){
                this.openState = false;
                this.optionsList.hide();
                if(this.input.val() === this.defaultTopic){
                    this.input.val("");
                    this.input.select();
                }
            },
            open: function(){
                this.openState = true;
                this.input.focus();
                this.input.select();
                this.optionsList.show();
            }
        });
    }
)

