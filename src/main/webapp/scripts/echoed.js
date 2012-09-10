var EventAggregator = _.extend({}, Backbone.Events);

Echoed = {
    Views: {
        Pages:{},
        Components:{}
    },
    Models:{},
    Collections:{},
    init: function() {
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
        var router = new Echoed.Router({EvAg: EventAggregator});
        var nav = new Echoed.Views.Components.Nav({EvAg: EventAggregator});
        var logout = new Echoed.Views.Components.Login({el: '#user', EvAg: EventAggregator});
        var infiniteScroll = new Echoed.Views.Components.InfiniteScroll({ el: '#infiniteScroll', EvAg : EventAggregator});
        var exhibit = new Echoed.Views.Pages.Exhibit({ el: '#content', EvAg: EventAggregator });
        var actions = new Echoed.Views.Components.Actions({ el: '#actions', EvAg: EventAggregator });
        var field = new Echoed.Views.Components.Field({ el: '#field', EvAg: EventAggregator });
        var story = new Echoed.Views.Components.Story({ el: '#story', EvAg: EventAggregator});
        var fade = new Echoed.Views.Components.Fade({ el: '#fade', EvAg: EventAggregator });
        var title = new Echoed.Views.Components.Title({ el: '#title', EvAg: EventAggregator });
        var pageTitle = new Echoed.Views.Components.PageTitle({ el: 'title', EvAg: EventAggregator});
        var category = new Echoed.Views.Components.Menu({ el: '#menu', EvAg: EventAggregator });
        var notifications = new Echoed.Views.Components.Notifications({ el: '#notifications-container', EvAg: EventAggregator });
        var categoryList = new Echoed.Views.Components.CategoryList({ el: '#category-nav', EvAg: EventAggregator });


        var iFrameComm = new Echoed.Views.Components.MessageHandler({ el: '#echoed-iframe', EvAg: EventAggregator });
        var iFrameNode = document.createElement('iframe');
        iFrameNode.height = "0px";
        iFrameNode.width = "0px";
        iFrameNode.style.border = "none";
        iFrameNode.id = "echoed-iframe";
        iFrameNode.src = Echoed.urls.api + "/echo/iframe";
        document.getElementsByTagName('body')[0].appendChild(iFrameNode);
        Backbone.history.start();
    }
};

