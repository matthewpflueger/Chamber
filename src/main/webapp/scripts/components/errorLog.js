define(
    [
        'jquery',
        'backbone',
        'underscore'
    ],
    function($, Backbone, _){
        return Backbone.View.extend({
            intialize: function(){
                window.onerror = function(message, file, line){
                    if(file.indexOf('echoed') >= 0){
                        var error = {
                            message: message,
                            file: file,
                            line: line,
                            location: location
                        };
                        var errorString = encodeURIComponent(JSON.stringify(error));
                        var xmlHttpRequest;
                        if (document.all) {
                            xmlHttpRequest = new ActiveXObject("Msxml2.XMLHTTP");
                        } else {
                            xmlHttpRequest = new XMLHttpRequest();
                        }

                        xmlHttpRequest.open("POST", Echoed.urls.api + "/posterror?error=" + errorString);
                        xmlHttpRequest.send();
                    }
                };
            }
        })
    }
)