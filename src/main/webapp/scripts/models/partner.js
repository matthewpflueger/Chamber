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
            },
            setPath: function(){

            },
            getPartnerPath: function(){
                var path = this.get('domain');
                if(this.has("page")){
                    path += "/" + this.get("page");
                }
                return path;
            }

        });
    }
)
