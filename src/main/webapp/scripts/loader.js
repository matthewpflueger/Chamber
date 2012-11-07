require.config({
    paths: {
        'jquery': 'libs/jquery-1.8.1.min',
        'requireLib': 'libs/require/require',
        'easyXDM': 'libs/easyXDM/easyXDM.min'

    }
});

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

        function echoedMessageHandler(message){
            var hash = window.location.hash;
            var index = hash.indexOf('echoed');
            if(index > 0){
                window.location.hash = hash.substr(0, index);
            }
            self.overlay.fadeOut();
        }

        function showEchoedOverlay(){
            var hash = window.location.hash;
            var index = hash.indexOf('echoed');
            if(index > 0){
                var iFrameHash = '';
                var hString = hash.substr(index);
                if(hString.split('_')[1]) iFrameHash = '#' + hString.split('_')[1];
                else iFrameHash = "#home";
                self.xdmOverlay.postMessage({
                    type: "hash",
                    data: iFrameHash
                });
                self.overlay.fadeIn();
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
                        "z-index": '99999',
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
                }
            });
            self.overlay = $('#echoed-overlay');

            $('#echoed-opener,.echoed-opener').live('click', function(){
                self.xdmOverlay.postMessage({ type: "hash", data: "home" });
                self.overlay.fadeIn();
            });

            if( EchoedSettings.opener === undefined){
                var open =$('<div></div>').attr("id","echoed-opener").append($('<img />').attr("src", EchoedSettings.urls.images +  "/bk_opener_dark_left.png").css({"display":"block"})).appendTo(body);
                open.css({
                    "left": "0px",
                    "top": "175px",
                    "position": "fixed",
                    "cursor": "pointer",
                    "box-shadow": "1px 1px 2px rgba(34,25,25,0.4)",
                    "-moz-box-shadow": "1px 1px 2px rgba(34,25,25,0.4)",
                    "-webkit-box-shadow": "1px 1px 2px rgba(34,25,25,0.4)"
                });
            }

            if(window.addEventListener) window.addEventListener('message', echoedMessageHandler, false);
            else window.attachEvent('onmessage', echoedMessageHandler);

            window.onhashchange = showEchoedOverlay;


            if($('#echoed-gallery').length > 0){
                var XDM1 = new easyXDM.Socket({
                    remote: EchoedSettings.urls.api +  "/widget/iframe/gallery?pid=" + parameters['pid'],
                    container: "echoed-gallery",
                    props: {
                        style: {
                            width: "100%",
                            overflow: "hidden",
                            display: "none"
                        },
                        id: "echoed-gallery-iframe"
                    },
                    onMessage: function(message, origin){
                        switch(message.type){
                            case 'load':
                                window.location.hash = "#echoed_" + message.data ;
                                break;
                            case 'resize':
                                $('#echoed-gallery-iframe').height(message.data).show();
                                break;
                        }
                    }
                });
            }

            showEchoedOverlay();

        });
    }
);