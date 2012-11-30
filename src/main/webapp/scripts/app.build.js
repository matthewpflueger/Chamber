({
    appDir: "./",
    baseUrl: "./",
    dir: "../scripts_build",
    keepBuildDir: false,
    paths: {
        'mobileConfig'      : 'jquery.mobile.config',

        'backbone'          : 'libs/backbone-0.9.2',
        'easyXDM'           : 'libs/easyXDM-2.4.16.3',
        'json2'             : 'libs/json2-20111019',
        'underscore'        : 'libs/underscore-1.4.1',

        'expanding'         : 'libs/jquery/expanding-20121116',
        'cloudinary'        : 'libs/jquery/jquery.cloudinary-1.0.1',
        'fileUploader'      : 'libs/jquery/jquery.fileupload-5.10.1',
        'iFrameTransport'   : 'libs/jquery/jquery.iframe-transport-1.4',
        'isotope'           : 'libs/jquery/jquery.isotope-1.5.21',
        'jqueryMobile'      : 'libs/jquery/jquery.mobile-1.2.0',
        'jquery'            : 'libs/jquery/jquery-1.8.3',
        'jqueryUI'          : 'libs/jquery/jquery-ui-1.9.1.custom',

        'requireLib'        : 'libs/require/require-2.1.1',
        'text'              : 'libs/require/text-2.0.3'
    },
    optimize: "uglify",
    modules: [
        {
            name: "loader",
            include: ['requireLib']
        },
        {
            name: "remote",
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
            name: "widget.preview",
            include: ['requireLib']
        },
        {
            name: "widget.gallery",
            include: ['requireLib']
        }
    ]
})
