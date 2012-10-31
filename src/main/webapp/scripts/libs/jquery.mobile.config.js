define(['jquery'], function ($) {
    $(document).on("mobileinit", function () {
        $.mobile.ajaxEnabled = false;
        $.mobile.linkBindingEnabled = false;
        $.mobile.hashListeningEnabled = false;
        $.mobile.pushStateEnabled = false;
        $.mobile.loadingMessage = false;
        $.event.special.swipe.scrollSupressionThreshold = 100;
        $.event.special.swipe.durationThreshold = 1000;
        $.event.special.swipe.horizontalDistanceThreshold = 175;
        $.event.special.swipe.verticalDistanceThreshold = 30;
    });
});