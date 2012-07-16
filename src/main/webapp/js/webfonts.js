WebFontConfig = {
    google: { families: ['Lato:300,400,700,900','Open+Sans:300italic,400italic,700italic,400,300,700','Arvo:400,700'] }
};
(function(){
    var wf= document.createElement('script');
    wf.src = ('https:' == document.location.protocol ? 'https' : 'http') +
        '://ajax.googleapis.com/ajax/libs/webfont/1/webfont.js';
    wf.type = 'text/javascript';
    wf.async = 'true';
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(wf, s);
})();

