define(
    [
        'jquery',
        'backbone',
        'underscore',
        'hgn!templates/select/select'
    ],
    function($, Backbone, _, templateSelect){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this,'render','click','open','selectOption','close');
                this.el = options.el;
                this.element = $(options.el);
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
                var template = templateSelect(self.options);
                self.element.html(template);
                if(this.locked !== true) self.element.addClass('input-select');
                this.input = $('#field-text-input');
                this.optionsList = $('#field-question-options-list');
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
                if(this.input.val() === this.options.freeForm) this.input.val("");
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

