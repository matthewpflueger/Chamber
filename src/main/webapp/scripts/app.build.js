    ({
    appDir: "./",
    baseUrl: "./",
    dir: "../scripts_build",
    keepBuildDir: false,
    mainConfigFile: 'app.config.js',
    optimize: "uglify",
    //stubModules can be used to remove unneeded plugins after build
    stubModules : ['text', 'hgn'],
    pragmasOnSave : {
        // you can use this pragma to exclude compiler logic from Hogan.js in
        // case you don't need to compile any templates after buid
        excludeHogan : true
    },
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
            name: "widget.preview",
            include: ['requireLib']
        },
        {
            name: "widget.gallery",
            include: ['requireLib']
        }
    ]
})
