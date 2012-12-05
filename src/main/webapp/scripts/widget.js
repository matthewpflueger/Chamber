require(
    [
        'requireLib',
        'jquery',
        'backbone',
        'underscore',
        'isotope',
        'components/errorLog',
        'components/infiniteScroll',
        'components/exhibit',
        'components/story',
        'components/input',
        'components/widget/messageHandler',
        'components/widgetCloser',
        'components/title',
        'components/login',
        'routers/widget',
        'models/user',
        'easyXDM',
        'isotopeConfig'
    ],
    function(requireLib, $, Backbone, _, isotope, ErrorLog, InfiniteScroll, Exhibit, Story, Input, MessageHandler, WidgetCloser, Title, Login, Router, ModelUser){

        $(document).ready(function(){
            this.EventAggregator = _.extend({}, Backbone.Events);

            this.properties = {
                urls: Echoed.urls,
                echoedUser: Echoed.echoedUser,
                partnerId: Echoed.partnerId,
                isWidget: true
            };

            //Initialize Models
            this.modelUser = new ModelUser(Echoed.echoedUser);

            this.modelUser.isLoggedIn();

            //Options
            this.options = function(el){
                var opt = {
                    properties: this.properties,
                    modelUser: this.modelUser,
                    EvAg: this.EventAggregator
                };
                if(el) opt.el = el;
                return opt;
            };

            this.errorLog = new ErrorLog(this.options());
            this.exhibit = new Exhibit(this.options('#exhibit'));
            this.infiniteScroll = new InfiniteScroll(this.options('#infiniteScroll'));
            this.input = new Input(this.options('#field-container'));
            this.story = new Story(this.options('#story-container'));
            this.closer = new WidgetCloser(this.options('#close'));
            this.titleNav = new Title(this.options('#title-container'));
            this.login = new Login(this.options("#login-container"));
            this.router = new Router(this.options());

            var iFrameNode = document.createElement('iframe');

            iFrameNode.height = '0px';
            iFrameNode.width = '0px';
            iFrameNode.style.border = "none";
            iFrameNode.id = "echoed-iframe";
            iFrameNode.src = Echoed.urls.api.replace("http://","https://") + "/echo/iframe";
            document.getElementsByTagName('body')[0].appendChild(iFrameNode);

            this.messageHandler = new MessageHandler(this.options('#echoed-iframe'));
            var socket = new easyXDM.Socket({
                onMessage: function(message, origin){
                    var msg = JSON.parse(message);
                    switch(msg.type){
                        case 'hash':
                            $('body').show();
                            window.location.hash = msg.data;
                            break;
                    }
                }
            });

            Backbone.history.start();
        });
    }
);

