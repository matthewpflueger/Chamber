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
                this.default = options.default;
                this.freeForm = options.freeForm;
                this.edit = options.edit;
                this.locked = options.locked;
                this.openState = false;
                this.render();
            },
            events: {
                "click .field-question-label" : "click",
                "click .field-question-option" : "selectOption",
                "keyup :input": "keyPress"
            },
            val: function(){
                if(this.locked !== true) return this.input.val();
                else return this.input.html();
            },
            render: function(){
                var self = this;


                self.label = $("<div class='field-question-label'></div>");


                if(this.locked !== true){
                    self.downArrow = $("<div class='downarrow'></div>");
                    self.label.append(self.downArrow);
                    self.element.addClass('input-select');
                    self.input = $("<input type='text' class='field-text-input'>");
                    self.optionsList = $("<div class='field-question-options-list'></div>").css("display", "none");
                    $.each(self.optionsArray, function(index, option){
                        self.options[index] = $("<div class='field-question-option'></div>").append(option);
                        self.optionsList.append(self.options[index]);
                    });
                    if(self.default !== null) self.input.val(self.default);
                    else self.input.val(self.options[0].text());

                    if(self.edit === false) self.input.attr('readonly', "true");

                    if(self.freeForm !== null) self.options[self.optionsArray.length] = $("<div class='field-question-option'></div>").append(self.freeForm);

                    self.optionsList.append(self.options[self.optionsArray.length]);


                } else{
                    self.input = $("<span></span>").text(self.default);
                }

                self.label.append(self.input);
                self.element.append(self.label);
                self.element.append(self.optionsList);

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
                if(this.input.val() === this.freeForm) this.input.val("");
                if(this.edit === true) this.input.select();
            },
            open: function(){
                if(this.locked !== true){
                    this.openState = true;
                    this.input.focus();
                    this.input.select();
                    this.optionsList.show();
                }
            }
        });
    }
)

