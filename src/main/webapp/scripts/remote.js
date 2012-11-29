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
        window.onhashchange = showEchoedOverlay;

        var parameters = gup(scriptUrl);
        var hash = window.location.hash;
        var body = $('body');
        body.append($('<link rel="stylesheet" type="text/css"/>').attr("href", EchoedSettings.urls.css + "/remote.css"));
        var loader = $('<div id="echoed-loader"></div>').appendTo(body).addClass('ech-top-left').addClass("ech-hor");
        self.preview = $('<div id="echoed-preview"></div>').appendTo(loader).css("display", "none");

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
                    "display": "none"
                }
            },
            onReady: function(){
                self.overlay = $('#echoed-overlay').css({ "z-index" : "999999999"});
                $('#echoed-opener,.echoed-opener, #ech-icon-container').live('click', function(){
                    self.overlay.fadeIn(function(){
                        $('html').css({"overflow": "hidden"});
                    });
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
                    "width" : "200px"
                }
            },
            onReady: function(){
                utils.AjaxFactory({
                    url: EchoedSettings.urls.api + "/api/partner/" + parameters['pid'],
                    dataType: 'json',
                    success: function(response){
                        self.stories = response.stories;
                        self.storyIndex = 0;
                        var i = 0, counter = 0;
                        while(i < self.stories.length && counter < 4){
                            var story = self.stories[i];
                            if(story.story.image) {
                                var link = $('<a></a>').attr("href", "#echoed_story/" + story.id).append($('<img />').attr("src",story.story.image.preferredUrl)).attr('index', i).addClass("echoed-story");
                                $('#echoed-options').append(link);
                                counter++;
                            }
                            i++;
                        }
                        $('#echoed-options').append($('<a></a>').attr("href","#echoed_write").append($('<span id="echoed-add"></span>')));
                        $('#echoed-add').live("mouseenter", function(){
                            self.xdmPreview.postMessage(JSON.stringify({ type: "text", data: "Add Your Story" }));
                        });
                        $('.echoed-story').live("mouseenter",function(){
                            var index = $(this).attr("index");
                            var msg = { type: "story", data: self.stories[index] };
                            self.xdmPreview.postMessage(JSON.stringify(msg));
                        });
                        $('.echoed-story').live("mouseleave",function(){
                            self.preview.hide();
                        });
                    }
                })();
                self.xdmPreview.postMessage(JSON.stringify({ type: "text", data: "Click Here to Share Your DIYs"}));
            },
            onMessage: function(message, origin){
                self.preview.show();
                window.setTimeout(function(){self.preview.hide();}, 2000);
            }
        });

        function echoedMessageHandler(message){

            if(message.data === "echoed-close"){
                var hash = window.location.hash;
                var index = hash.indexOf('echoed');
                if(index > 0){
                    window.location.hash = hash.substr(0, index);
                }
                self.overlay.fadeOut(function(){
                    $('html').css({"overflow": "auto"});
                });
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
                self.overlay.fadeIn(function(){
                    $('html').css({"overflow": "hidden"});
                });
                self.xdmOverlay.postMessage(msg);

            }
        }
    }
);