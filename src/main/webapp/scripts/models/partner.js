define(
    [
        'backbone'
    ],
    function(Backbone){
        return Backbone.Model.extend({
            isEchoed: function(){
                if(this.has("name")){
                    if(this.get("name") === "Echoed"){
                        return true;
                    }
                }
                return false;
            }
        });
    }
)
