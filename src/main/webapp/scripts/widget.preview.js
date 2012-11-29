require(
    [
        'requireLib',
        'jquery',
        'easyXDM'
    ],
    function(requireLib, $){
        $(document).ready(function(){
            var self = this;
            self.img = $("#content-image");
            self.contentText = $("#content-text");
            self.socket = new easyXDM.Socket({
                onMessage: function(message, origin){
                    try{
                        var msg = JSON.parse(message);
                        switch(msg.type){
                            case "story":
                                var story = msg.data;
                                if(story.story.image) self.img.attr("src", story.story.image.preferredUrl);
                                self.img.show();
                                self.contentText.text(story.story.title);
                                break;
                            case "text":
                                var text = msg.data;
                                self.img.hide();
                                self.contentText.text(text);
                                break;
                        }
                    } catch(e){
                    }
                    self.socket.postMessage("show");
                }
            });
        });
    });