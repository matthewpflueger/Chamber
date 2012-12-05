require(
    [
        'requireLib',
        'jquery',
        'underscore',
        'backbone',
        'isotope',
        'routers/website',
        'components/exhibit',
        'components/story',
        'components/pageTitle',
        'components/title',
        'components/input',
        'components/user',
        'components/messageHandler',
        'components/notifications',
        'components/errorLog',
        'components/actions',
        'components/infiniteScroll',
        'components/nav',
        'components/login',
        'models/user',
        'isotopeConfig'
    ],
    function(require, $, _, Backbone, isotope, Router, Exhibit, Story, PageTitle, Title, Input, User, MessageHandler, Notifications, ErrorLog, Actions, InfiniteScroll, Nav, Login, ModelUser){

        $(document).ready(function(){
            this.EventAggregator = _.extend({}, Backbone.Events);
            this.properties = {
                urls: Echoed.urls,
                exhibitShowLogin: true
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
            this.router = new Router(this.options());
            this.infiniteScroll = new InfiniteScroll(this.options('#infiniteScroll'));
            this.nav = new Nav(this.options());
            this.story = new Story(this.options('#story-container'));
            this.exhibit = new Exhibit(this.options('#content'));
            this.pageTitle = new PageTitle(this.options('title'));
            this.contentTitle = new Title(this.options('#title-container'));
            this.actions = new Actions(this.options('#actions'));
            this.input = new Input(this.options('#field-container'));
            this.user = new User(this.options('#user'));
            this.notifications = new Notifications(this.options("#notifications-container"));
            this.login = new Login(this.options("#login-container"));


            var iFrameNode = document.createElement('iframe');

            iFrameNode.height = '0px';
            iFrameNode.width = '0px';
            iFrameNode.style.border = "none";
            iFrameNode.id = "echoed-iframe";
            iFrameNode.src = Echoed.urls.api + "/echo/iframe";
            document.getElementsByTagName('body')[0].appendChild(iFrameNode);
            this.messageHandler = new MessageHandler(this.options('#echoed-iframe'));

            Backbone.history.start();
            }
        );
    }
);

