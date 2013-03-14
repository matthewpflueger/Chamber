define(
    [
        'backbone'
    ],
    function(Backbone){
        return Backbone.Model.extend({
            baseUrl: function(){
                if(this.has("contextType") && this.has("id")){
                    if (this.get("contextType") === "me" && this.id !== "feed"){
                        return this.get("contextType").toLowerCase() + "/"
                    } else {
                        return this.get("contextType").toLowerCase() + "/" + this.id + "/";
                    }
                } else {
                    return "explore/";
                }
            },
            getContentType: function(){
                if(this.has("contentType")) {
                    return this.get("contentType");
                } else {
                    return null;
                }
            },
            getContentTypeSinguilar: function(){
                if(this.has("contentType")) {
                    return this.get("contentType").singular;
                } else {
                    return null;
                }
            },
            setPage: function(page){
                this.set("page", page);
            },
            getPage: function(){
                if(this.has("page")) {
                    return this.get("page");
                } else {
                    return null;
                }
            }
        })
    }
);