define(
    [
        'backbone',
        'models/story',
        'components/utils'
    ],
    function(Backbone, ModelContent, utils){
        return Backbone.Collection.extend({
            model: ModelContent,
            initialize: function(models, options){
            },
            nextItem: function(){
            },
            previousItem: function(){
            }
        });
    }
);