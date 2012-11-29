require(
    [
        'requireLib',
        'jquery',
        'easyXDM',
        'components/remote/remote',
        'components/utils'
    ],
    function(require, $, _, Remote, utils){

        var self = this;

        var scriptUrl = "";

        function gup(a){
            var b = a.split("?");
            if(b.length === 0) return {};

            else {
                var c = b[1].split("&");
                var d = {};
                for(var i = 0; i < c.length; i++){
                    var e = c[i].split("=");
                    d[e[0]] = e[1];
                }
                return d;
            }
        }

        if($('script[data-main*="remote.js"]').length > 0) scriptUrl = $('script[data-main*="remote.js"]').attr("data-main");
        else if($('script[src*="remote.js"]').length > 0) scriptUrl = $('script[src*="remote.js"]').attr('src');

        if(window.addEventListener) window.addEventListener('message', echoedMessageHandler, false);
        else window.attachEvent('onmessage', echoedMessageHandler);

        var parameters = gup(scriptUrl);
        var hash = window.location.hash;
        var body = $('body');
        body.append($('<link rel="stylesheet" type="text/css"/>').attr("href", EchoedSettings.urls.css + "/remote.css"));
        var loader = $('<div id="echoed-loader"></div>').addClass('ech-topleft').appendTo(body);
        var preview = $('<div id="echoed-preview"></div>').appendTo(body).css({
            position: "fixed",
            left: "0px",
            top: "45px"
        });
        self.remote = new Remote({ el: "#echoed-loader" });


        self.xdmOverlay = new easyXDM.Socket({
            remote: EchoedSettings.urls.api +  "/widget/iframe/?pid=" + parameters['pid'],
            container: document.getElementsByTagName('body')[0],
            props: {
                id: "echoed-overlay",
                style: {
                    "top": "0px",
                    "left": "0px",
                    "right": "0px",
                    "bottom": "0px",
                    "height": "100%",
                    "width": "100%",
                    "position":"fixed",
                    "overflow-y":"scroll",
                    "display": "none"

                }
            },
            onReady: function(){
                self.overlay = $('#echoed-overlay').css({ "z-index" : "999999999"});
                $('#echoed-opener,.echoed-opener, #echoed-loader').live('click', function(){
                    self.overlay.fadeIn();
                    self.xdmOverlay.postMessage(JSON.stringify({ type: "hash", data: "home" }));
                });
                showEchoedOverlay();
            }
        });

        self.xdmPreview = new easyXDM.Socket({
            remote: EchoedSettings.urls.api + "/widget/iframe/preview?pid=" + parameters['pid'],
            container: "echoed-preview",
            props:{
                style: {
                    "background-color" : "rgba(0, 0, 0, 0.8)",
                    "border-radius" : "5px",
                    "height" : "45px",
                    "width" : "250px"
                }
            },
            onReady: function(){
                utils.AjaxFactory({
                    url: EchoedSettings.urls.api + "/api/partner/" + parameters['pid'],
                    dataType: 'json',
                    success: function(response){
                        self.stories = response.stories;
                        self.storyIndex = 0;
                        $.each(self.stories, function(index, story){
                            if(index <= 3) if(story.story.image) $('#echoed-options').append($('<img />').attr("src",story.story.image.preferredUrl));
                        });
                        window.setInterval(function(){
                            if(self.storyIndex >= self.stories.length) self.storyIndex = 0;
                            preview.fadeOut(function(){
                                self.xdmPreview.postMessage(JSON.stringify(self.stories[self.storyIndex]));
                                self.storyIndex++;
                            });
                        }, 6000);
                    }
                })();
            },
            onMessage: function(message, origin){
                preview.fadeIn();
            }
        });

        function echoedMessageHandler(message){

            if(message.data === "echoed-close"){
                var hash = window.location.hash;
                var index = hash.indexOf('echoed');
                if(index > 0){
                    window.location.hash = hash.substr(0, index);
                }
                self.overlay.fadeOut();
            }
        }

        function showEchoedOverlay(){
            var hash = window.location.hash;
            var index = hash.indexOf('echoed');
            if(index > 0){
                var iFrameHash = '';
                var hString = hash.substr(index);
                if(hString.split('_')[1]) iFrameHash = '#' + hString.split('_')[1];
                else iFrameHash = "#home";
                var msg = JSON.stringify({
                    "type": "hash",
                    "data": iFrameHash
                });
                self.overlay.fadeIn();
                self.xdmOverlay.postMessage(msg);

            }
        }




    }
);