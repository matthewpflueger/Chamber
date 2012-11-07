({
    appDir: "./",
    baseUrl: "./",
    dir: "../scripts_build",
    keepBuildDir: false,
    paths: {
        'requireLib': 'libs/require/require',
        'json2' : 'libs/json2',
        'underscore' : 'libs/underscore-amd-1.4.1-min',
        'backbone' : 'libs/backbone-amd-0.9.2-min',
        'text' : 'libs/require/text',
        'jquery' : 'libs/jquery-1.8.1.min',
        'jqueryMobile': 'libs/jquery.mobile-1.1.1.min',
        'jqueryMobileConfig': 'libs/jquery.mobile.config',
        'jqueryUI': 'libs/jquery-ui-1.9.1.custom.min',
        'isotope' : 'libs/jquery.isotope.min',
        'imagesLoaded' : 'libs/imagesloaded',
        'fileuploader' : 'libs/fileuploader',
        'expanding' : 'libs/expanding',
        'easyXDM' : 'libs/easyXDM/easyXDM.debug'
    },
    shim: {
        'jqueryMobileConfig': ['jquery'],
        'jqueryUI': ['jquery'],
        'jqueryMobile': ['jquery', 'jqueryMobileConfig']
    },
    optimize: "uglify",
    modules: [
        {
            name: "loader",
            include: ['requireLib']
        },
        {
            name: 'mobile',
            include: ['requireLib']
        },
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
        },
        {
            name: "admin",
            include: ['requireLib']
        },
        {
            name: "static",
            include: ['requireLib']
        },
        {
            name: "widget.gallery",
            include: ['requireLib']

        }
    ]
})
