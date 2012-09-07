({
    appDir: "./",
    baseUrl: "./",
    dir: "../release",
    paths: {
        'json2' : 'libs/json2',
        'underscore' : 'libs/underscore-min',
        'backbone' : 'libs/backbone-0.9.1-min',
        'text' : 'libs/require/text',
        'jquery' : 'libs/jquery-1.8.0.min',
        'isotope' : 'libs/jquery.isotope.min',
        'imagesLoaded' : 'libs/imagesloaded',
        'fileuploader' : 'libs/fileuploader',
        'expanding' : 'libs/expanding',
        'requireLib': 'libs/require/require'
    },
    shim: {
        underscore: {
            exports: "_"
        },
        backbone: {
            deps: ['underscore', 'jquery'],
            exports: "Backbone"
        }
    },
    optimize: "uglify",
    modules: [
        {
            name: "main",
            include: ['requireLib']
        }
    ]
})