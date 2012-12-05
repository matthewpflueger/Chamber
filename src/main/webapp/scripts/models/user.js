define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.Model.extend({
            is: function(id){
                if(this.get('id') === id || this.get('handle') === id) return true;
            },
            isLoggedIn: function(){
                if(this.has('id')) return true;
            },
            login: function(echoedUser){
                this.set(echoedUser)
            }
        })
    }
);