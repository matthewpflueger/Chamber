({
    appDir: "./",
    baseUrl: "./",
    dir: "../scripts_build",
    keepBuildDir: false,
    mainConfigFile: 'app.config.js',
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
