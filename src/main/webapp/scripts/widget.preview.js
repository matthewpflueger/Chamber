require(
    [
        'requireLib',
        'jquery',
        'easyXDM',
        'components/utils'
    ],
    function(requireLib, $, easyXDM, utils){
        $(document).ready(function(){
            var self = this;
            self.img = $('#image-container');
            self.contentText = $("#content-text");
            self.contentTextContainer = $('#content-text-container');
            self.socket = new easyXDM.Socket({
                onMessage: function(message, origin){
                    try{
                        var msg = JSON.parse(message);
                        switch(msg.type){
                            case "story":
                                var story = msg.data;
                                if(story.story.image) self.img.html(utils.fill(story.story.image, 40, 40));
                                self.img.show();
                                self.contentText.text(story.story.title);
                                self.contentText.removeClass("text-only");
                                break;
                            case "text":
                                var text = msg.data;
                                self.img.hide();
                                self.contentText.text(text);
                                self.contentTextContainer.addClass("text-only");
                                break;
                        }
                    } catch(e){
                    }
                    self.socket.postMessage("show");
                }
            });
        });
    });