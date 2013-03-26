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
        'views/item/item',
        'components/input',
        'components/widget/messageHandler',
        'components/widgetCloser',
        'components/title',
        'components/login',
        'components/user',
        'components/nav',
        'views/page/page',
        'views/header/header',
        'views/background/background',
        'routers/app',
        'models/user',
        'models/context',
        'models/partner',
        'easyXDM',
        'isotopeConfig'
    ],
    function(requireLib,
             $,
             Backbone,
             _,
             isotope,
             ErrorLog,
             InfiniteScroll,
             Exhibit,
             Item,
             Input,
             MessageHandler,
             WidgetCloser,
             Title,
             Login,
             User,
             Nav,
             Page,
             Header,
             Background,
             Router,
             ModelUser,
             ModelContext,
             ModelPartner,
             easyXDM){

        $(document).ready(function(){
            this.EventAggregator = _.extend({}, Backbone.Events);
            this.urls = Echoed.urls;

            //Initialize Models
            this.modelUser = new ModelUser(Echoed.echoedUser,   { urls: this.urls });
            this.modelContext = new ModelContext({},            { urls: this.urls });
            this.modelPartner = new ModelPartner({ name: "Echoed" },            { urls: this.urls });

            this.properties = {
                urls: this.urls,
                echoedUser: Echoed.echoedUser,
                isOverlay:  true
            };

            if(Echoed.partnerId){
                var hashUrl = "partner/" + Echoed.partnerId;
                if(Echoed.path) {
                    hashUrl += "/page" + Echoed.path;
                }
                window.location.hash = hashUrl;
            }

            //Options
            this.options = function(el){
                var opt = {
                    properties:     this.properties,
                    modelUser:      this.modelUser,
                    modelContext:   this.modelContext,
                    modelPartner:   this.modelPartner,
                    EvAg:           this.EventAggregator
                };
                if(el) opt.el = el;
                return opt;
            };
            this.errorLog =         new ErrorLog(this.options());
            this.exhibit =          new Exhibit(this.options('#exhibit'));
            this.infiniteScroll =   new InfiniteScroll(this.options('#infiniteScroll'));
            this.nav =              new Nav(this.options());
            this.input =            new Input(this.options('#field-container'));
            this.item =             new Item(this.options('#item-container'));
            this.titleNav =         new Title(this.options('#title-container'));
            this.login =            new Login(this.options("#login-container"));
            this.page =             new Page(this.options());
            this.router =           new Router(this.options());
            this.header =           new Header(this.options("#header-container"));
            this.background =       new Background(this.options("#background"));

            var iFrameNode = document.createElement('iframe');

            iFrameNode.height = '0px';
            iFrameNode.width = '0px';
            iFrameNode.style.border = "none";
            iFrameNode.id = "echoed-iframe";
            iFrameNode.src = Echoed.https.site + "/echo/iframe";
            document.getElementsByTagName('body')[0].appendChild(iFrameNode);

            this.messageHandler = new MessageHandler(this.options('#echoed-iframe'));

            Backbone.history.start({ pushState: true });

            $(document).delegate("a", "click", function(evt) {
                // Get the anchor href and protcol
                var href = $(this).attr("href");
                var protocol = this.protocol + "//";

                // Ensure the protocol is not part of URL, meaning its relative.
                // Stop the event bubbling to ensure the link will not cause a page refresh.
                if (href.slice(protocol.length) !== protocol) {
                    evt.preventDefault();

                    // Note by using Backbone.history.navigate, router events will not be
                    // triggered.  If this is a problem, change this to navigate on your
                    // router.
                    Backbone.history.navigate(href, true);
                }
            });
        });
    }
);

