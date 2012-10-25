require.config({
    paths: {
        'jquery': 'libs/jquery-1.8.1.min',
        'requireLib': 'libs/require/require'
    }
});

require(
    [
        'requireLib',
        'jquery'
    ],
    function(require, $){
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
           $('#echoed-overlay').fadeOut();
        }

        function showEchoedOverlay(overlay){
            overlay.get(0).contentWindow.postMessage("echoed-open","*");
            overlay.fadeIn();
        }

        $(document).ready(function(){
            var scriptUrl = $('script[data-main*="loader.js"]') ? $('script[data-main*="loader.js"]').attr("data-main") : $('script[src*="loader.js"]').attr('src');
            var parameters = gup(scriptUrl);
            var hash = window.location.hash;
            var iFrameHash = "";
            var index = hash.indexOf('echoed');
            if(index > 0){
                var hString = hash.substr(index);
                if(hString.split('_')[1]) iFrameHash = '#' + hString.split('_')[1];
                else iFrameHash = "#home";
            }
            var body = $('body');
            var echoedOverlay = $('<iframe></iframe>').attr("id","echoed-overlay");
            var src = "http://localhost.com:8080/widget/iframe?pid=" + parameters['pid'] + iFrameHash;
            echoedOverlay.css({
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
            }).attr('src', src);
            body.append(echoedOverlay);

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

            $('#echoed-opener,.echoed-opener').live('click', function(){
                showEchoedOverlay(echoedOverlay);
            });
            if(index > 0) showEchoedOverlay(echoedOverlay);

            window.onhashchange = function(){
                if(window.location.hash.indexOf("echoed") > 0) showEchoedOverlay(echoedOverlay);
            }
        });
    }
);