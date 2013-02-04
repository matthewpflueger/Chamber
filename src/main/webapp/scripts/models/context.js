define(
    [
        'backbone'
    ],
    function(Backbone){
        return Backbone.Model.extend({
            baseUrl: function(){
                if(this.has("contextType") && this.has("id")){
                    return this.get("contextType").toLowerCase() + "/" + this.id + "/";
                } else {
                    return false;
                }

            }
        })
    }
);