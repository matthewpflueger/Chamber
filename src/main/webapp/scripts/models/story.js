define(
    [
        'backbone',
        'components/utils'
    ],
    function(Backbone, utils) {
        return Backbone.Model.extend({
            moderate: function(baseUrl) {
                var id = this.get("id");
                var storyOwnerId = this.get("echoedUser").id;
                var isModerated = !this.get("isModerated");
                this.set("isModerated", isModerated);
                utils.AjaxFactory({
                    url: baseUrl + "/story/" + id + "/moderate",
                    type: "POST",
                    data: {
                        moderated: isModerated,
                        storyOwnerId : storyOwnerId
                    },
                    success: function(data){
                    }
                })();
            },
            isIncomplete: function(){
                return this.get("chapters").length == 0
            },
            getImageCount: function(){
                return 0;
            },
            getCoverImage: function(){
                if(this.get("story").image) return this.get("story").image;
                else if (this.get("chapterImages").length > 0) return this.get("chapterImages")[0].image;
                else return null;
            }
        });
    }
);