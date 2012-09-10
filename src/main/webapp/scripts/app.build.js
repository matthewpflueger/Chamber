({
    appDir: "./",
    baseUrl: "./",
    dir: "../release",
    paths: {
        'requireLib': 'libs/require/require',
        'json2' : 'libs/json2',
        'underscore' : 'libs/underscore-amd-1.3.3-min',
        'backbone' : 'libs/backbone-amd-0.9.2-min',
        'text' : 'libs/require/text',
        'jquery' : 'libs/jquery-1.8.0.min',
        'isotope' : 'libs/jquery.isotope.min',
        'imagesLoaded' : 'libs/imagesloaded',
        'fileuploader' : 'libs/fileuploader',
        'expanding' : 'libs/expanding'
    },
//    shim: {
//        underscore: {
//            exports: "_"
//        },
//        backbone: {
//            deps: ['underscore', 'jquery'],
//            exports: "Backbone"
//        }
//    },
    optimize: "uglify",
    modules: [
        {
            name: "widget",
            include: ['requireLib']
        },
        {
            name: "main",
            include: ['requireLib']
        }

    ]
})