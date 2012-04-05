function getQueryVariable(string, variable){
    var vars = string.split("&");
    for ( var i = 0; i < vars.length; i++){
        var pair = vars[i].split("=");
        if(pair[0]== variable){
            return decodeURIComponent(pair[1]);
        }
    }
    return null;
}

$(document).ready(function(){

    var popup = $('#echoed-popup');
    popup.html('');

    var close = $('<img src="http://demo.echoed.com/images/btn_closewin.png"/>').attr("id","echoed-close");
    popup.append(close);

    var div = '<div></div>';
    var popMargTop = (0) ;
    var popMargLeft = (360);
    popup.css({
        'top' : 50,
        'margin-left' : -popMargLeft
    });
    $('body').append('<div id="echoed-fade"></div>'); //Add the fade layer to bottom of the body tag.
    $('#echoed-fade').css({'filter' : 'alpha(opacity=80)'}).fadeIn();

    var c = $(div).attr("id","echoed-container");
    var et = $(div).attr("id","echoed-hiw-t").html('GET UP TO {{maxPercentage}}% BACK WHEN YOU SHARE YOUR PURCHASE');
    var ett = $(div).attr("id","echoed-hiw-tt").html("Share the products you've purchased with friends and get more money back with each click!");
    var pcc = $(div).attr("id","echoed-pcc").addClass("clearfix");
    var container = $(div).attr("id","echoed-p-c").addClass("clearfix");
    pcc.append(container);
    c.append(et).append(pcc).append(ett);
    c.appendTo(popup);

    var count = 0;


    $('img[src^="http://demo.echoed.com/echo/button"]').each(function(){
        count++;
        if(count <=3){
            container.css('width',count * 250);
        }
        var pair = $(this).attr("src").split("?");
        var queryString = pair[1];
        var imageUrl = getQueryVariable(queryString, 'imageUrl');
        var brand = getQueryVariable(queryString,'brand');
        var name = getQueryVariable(queryString,'productName');
        var p = $(div).addClass("echoed-p").attr("href","http://demo.echoed.com/echo?" + queryString);
        var bs = $(div).addClass("echoed-bs");
        var ic = $(div).addClass("echoed-i-c").append($('<img/>').attr("src",imageUrl).addClass("echoed-i"));
        var t = $(div).addClass("echoed-t").html('<strong>' + brand + '</strong></br>' + name);
        p.append(bs).append(ic).append(t);

        container.append(p);
    });
    $('.echoed-p').live('mouseenter', function(){
        $(this).find('.echoed-bs').addClass("current");
    });

    $('.echoed-p').live('mouseleave', function(){
        $(this).find('.echoed-bs').removeClass("current");
    });

    $('.echoed-p').live('click', function(){
        var url = $(this).attr("href");
        window.open(url,'Echoed','width=800,height=440,toolbar=0,menubar=0,location=0,status=1,scrollbars=0,resizable=0,left=0,top=0');
    });

    $('#echoed-close').live('click', function(){
        $('#echoed-fade,#echoed-popup').fadeOut();
    });
    
    popup.appendTo($('body')).fadeIn();

});

