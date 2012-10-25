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

        $(document).ready(function(){
            var scriptUrl = $('script[data-main*="loader.js"]') ? $('script[data-main*="loader.js"]').attr("data-main") : $('script[src*="loader.js"]').attr('src');
            var parameters = gup(scriptUrl);

        });
    }
);