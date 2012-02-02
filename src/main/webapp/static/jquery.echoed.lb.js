$(document).ready(function(){

    var popup = $('#echoed-popup').appendTo('body').fadeIn();
    var popMargTop = (0) ;
    var popMargLeft = (360);
    popup.css({
        'top' : 50,
        'margin-left' : -popMargLeft
    });
    $('body').append('<div id="echoed-fade"></div>'); //Add the fade layer to bottom of the body tag.
    $('#echoed-fade').css({'filter' : 'alpha(opacity=80)'}).fadeIn();
    $('#echoed-fade,#echoed-popup').live('click', function(){
        $('#echoed-fade,#echoed-popup').fadeOut();
    });
});

