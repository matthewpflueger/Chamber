require(
    [
        'requireLib',
        'jquery',
        'easyXDM'
    ],
    function(require, $){

        var self = this;

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

        $(document).ready(function(){
            var scriptUrl = "";
            if($('script[data-main*="loader.js"]').length > 0){
                scriptUrl = $('script[data-main*="loader.js"]').attr("data-main");
            } else if($('script[src*="loader.js"]').length > 0){
                scriptUrl = $('script[src*="loader.js"]').attr('src');
            }
            var parameters = gup(scriptUrl);
            var hash = window.location.hash;
            var body = $('body');
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
                    $('#echoed-opener,.echoed-opener').live('click', function(){
                        self.overlay.fadeIn();
                        self.xdmOverlay.postMessage(JSON.stringify({ type: "hash", data: "home" }));
                    });
                    showEchoedOverlay();
                }
            });

            if(window.addEventListener) window.addEventListener('message', echoedMessageHandler, false);
            else window.attachEvent('onmessage', echoedMessageHandler);

            window.onhashchange = showEchoedOverlay;

            if(EchoedSettings.useGallery === true && $('#echoed-gallery').length === 0) $('<div id="echoed-gallery"></div>').appendTo($('body'));

            if($('#echoed-gallery').length > 0){
                self.echoedGallery  = $('#echoed-gallery').css({
                    position: "fixed",
                    left: "0px",
                    right: "0px",
                    bottom: "0px",
                    "border-top": "1px solid #acacac",
                    "box-shadow": "0px -3px 5px rgba(34,25,25,0.2)",
                    "z-index" : "1000000"
                })



                var XDM1 = new easyXDM.Socket({
                    remote: EchoedSettings.urls.api +  "/widget/iframe/gallery?pid=" + parameters['pid'],
                    container: "echoed-gallery",
                    props: {
                        style: {
                            width: "100%",
                            overflow: "hidden",
                            height: "0px",
                            display: "block"
                        },
                        id: "echoed-gallery-iframe"
                    },
                    onMessage: function(message, origin){
                        var msg = JSON.parse(message);
                        switch(msg.type){
                            case 'load':
                                window.location.hash = "#echoed_" + msg.data ;
                                break;
                            case 'resize':
                                $('#echoed-gallery-iframe').hide().height(msg.data).slideDown();
                                break;
                            case 'close':
                                self.echoedGallery.slideUp();
                        }
                    }
                });
            } else {
                var open = '';
                if( EchoedSettings.opener === undefined && $('#echoed-opener').length === 0) open = $('<div></div>').attr("id","echoed-opener").append($('<img />').attr("src", EchoedSettings.urls.images +  "/bk_opener_dark_left.png").css({"display":"block"})).appendTo(body);
                else open = $('#echoed-opener');
                open.find('img').css({"display": "block"});
                open.css({
                    "top": "175px",
                    "position": "fixed",
                    "cursor": "pointer",
                    "box-shadow": "1px 1px 2px rgba(34,25,25,0.4)",
                    "-moz-box-shadow": "1px 1px 2px rgba(34,25,25,0.4)",
                    "-webkit-box-shadow": "1px 1px 2px rgba(34,25,25,0.4)"
                });

                if(EchoedSettings.openerSide === 'right') open.css({ "right" : "0px" });
                else open.css({ "left": "0px" });

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