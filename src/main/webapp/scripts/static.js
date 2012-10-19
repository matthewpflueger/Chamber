require.config({
    paths: {
        'backbone': 'libs/backbone-amd-0.9.2-min',
        'underscore': 'libs/underscore-amd-1.4.1-min',
        'jquery': 'libs/jquery-1.8.1.min',
        'isotope': 'libs/jquery.isotope.min',
        'expanding' : 'libs/expanding',
        'fileuploader': 'libs/fileuploader',
        'text': 'libs/require/text',
        'requireLib': 'libs/require/require',
        'easyXDM': 'libs/easyXDM.debug.js'
    },
    shim: {
        fileuploader: {
            exports: 'qq'
        }
    }
});


require(
    [
        'requireLib',
        'jquery'
    ],
    function(require, $){
        $('.placeholder-input').keyup(function(e){
            if($(this).val() !== "") $(this).addClass("on");
            else $(this).removeClass("on")
        });
    }
);
