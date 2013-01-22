define(
    [
        'backbone',
        'models/story',
        'components/utils'
    ],
    function(Backbone, ModelContent, utils){
        return Backbone.Collection.extend({
            initialize: function(models, options){
                this.url = options.url;
            },
            nextPage: function(){

            },
            nextItem: function(){
            },
            previousItem: function(){
            }
        });
    }
);