require.config({
    paths: {
        'backbone': 'libs/backbone-amd-0.9.2-min',
        'underscore': 'libs/underscore-amd-1.4.1-min',
        'jquery': 'libs/jquery-1.8.1.min',
        'text': 'libs/require/text',
        'requireLib': 'libs/require/require',
        'easyXDM': 'libs/easyXDM/easyXDM.min'
    },
    shim: {
        fileuploader: {
            exports: 'qq'
        }
    }
});

require(
    [
        'requireLib',
        'jquery',
        'backbone',
        'underscore',
        'components/utils',
        'easyXDM'
    ],
    function(requireLib, $, Backbone, _, utils){
        $(document).ready(function(){

            var properties = {
                urls: Echoed.urls,
                echoedUser: Echoed.echoedUser,
                exhibitShowLogin: true
            };

            var socket = new easyXDM.Socket({});
            var container = $('#gallery-container');
            var width = 220;

            $('.gallery-image-container').live('click', function(){
                var id = $(this).attr('id');
                socket.postMessage(JSON.stringify({
                    'type': 'load',
                    'data': 'story/' + id
                }));
            });

            $('#gallery-prev').live('click', function(){
                var left = container.position().left;
                if(left < 0){
                    container.animate({
                        left: Math.min(left + width, 0)
                    }, 'fast');
                }
            });


            $('#gallery-next').live('click', function(){
                container.animate({
                    left: '-=' + width
                }, 'fast');
            });

            utils.AjaxFactory({
                url: properties.urls.api + "/api/partner/" + Echoed.partnerId,
                dataType: 'json',
                success: function(response){
                    $.each(response.stories, function(index, storyFull){
                        if(storyFull.story.image || storyFull.chapters[0].image){
                            var image = storyFull.story.image ? storyFull.story.image : storyFull.chapters[0].image;
                            var gic = $('<div></div>').addClass('gallery-image-container').attr("id", storyFull.id);
                            $('<img />').attr('src', image.preferredUrl).css(utils.getMinImageSizing(image, 75, 55)).appendTo(gic);
                            gic.appendTo(container);
                            container.width(container.width() + width);
                        }
                    });
                    socket.postMessage(JSON.stringify({
                        "type" : "resize",
                        "data" : document.body.scrollHeight
                    }));
                }
            })();

            $('#close').live('click', function(){
                socket.postMessage(JSON.stringify({
                    "type" : "close"
                }));
            });

            $('#share').live('click', function(){
                socket.postMessage(JSON.stringify({
                    "type" : "load",
                    "data" : "write"
                }))
            });

            $('#view').live('click', function(){
                socket.postMessage(JSON.stringify({
                    "type" : "load",
                    "data" : ""
                }))
            })
        });
    }
);