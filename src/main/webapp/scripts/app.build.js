({
    appDir: "./",
    baseUrl: "./",
    dir: "../scripts_build",
    keepBuildDir: false,
    paths: {
        'requireLib': 'libs/require/require',
        'json2' : 'libs/json2',
        'underscore' : 'libs/underscore-amd-1.3.3-min',
        'backbone' : 'libs/backbone-amd-0.9.2-min',
        'text' : 'libs/require/text',
        'jquery' : 'libs/jquery-1.8.1.min',
        'isotope' : 'libs/jquery.isotope.min',
        'imagesLoaded' : 'libs/imagesloaded',
        'fileuploader' : 'libs/fileuploader',
        'expanding' : 'libs/expanding',
        'easyXDM' : 'libs/easyXDM/easyXDM.debug.js'
    },
    optimize: "uglify",
    modules: [
        {
            name: "widget",
            include: ['requireLib']
        },
        {
            name: "main",
            include: ['requireLib']
        },
        {
            name: "partner",
            include: ['requireLib']
        }
    ]
})