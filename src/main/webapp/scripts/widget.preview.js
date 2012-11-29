require(
    [
        'requireLib',
        'jquery',
        'easyXDM'
    ],
    function(requireLib, $){
        $(document).ready(function(){
            var self = this;
            var img = $("#content-image");
            var text = $("#content-text");
            self.socket = new easyXDM.Socket({
                onMessage: function(message, origin){
                    var story = JSON.parse(message);
                    if(story.story.image) img.attr("src", story.story.image.preferredUrl);
                    text.text(story.story.title);
                    self.socket.postMessage("show");
                }
            });
        });
    });