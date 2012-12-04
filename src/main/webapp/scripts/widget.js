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
        'easyXDM'
    ],
    function(requireLib, $, Backbone, _, isotope, ErrorLog, InfiniteScroll, Exhibit, Story, Input, MessageHandler, WidgetCloser, Title, Login, Router, ModelUser){

        $.Isotope.prototype._getCenteredMasonryColumns = function() {
            this.width = this.element.width();

            var parentWidth = this.element.parent().width();

            // i.e. options.masonry && options.masonry.columnWidth
            var colW = this.options.masonry && this.options.masonry.columnWidth ||
                // or use the size of the first item
                this.$filteredAtoms.outerWidth(true) ||
                // if there's no items, use size of container
                parentWidth;

            var cols = Math.floor( parentWidth / colW );
            cols = Math.max( cols, 1 );

            // i.e. this.masonry.cols = ....
            this.masonry.cols = cols;
            // i.e. this.masonry.columnWidth = ...
            this.masonry.columnWidth = colW;
            //Title Container Resizing
            $('#title-container').animate({
                width: Math.max(cols * colW - 10, 890)
            });
        };

        $.Isotope.prototype._masonryReset = function() {
            // layout-specific props
            this.masonry = {};
            // FIXME shouldn't have to call this again
            this._getCenteredMasonryColumns();
            var i = this.masonry.cols;
            this.masonry.colYs = [];
            while (i--) {
                this.masonry.colYs.push( 0 );
            }
        };

        $.Isotope.prototype._masonryResizeChanged = function() {
            var prevColCount = this.masonry.cols;
            // get updated colCount
            this._getCenteredMasonryColumns();
            return ( this.masonry.cols !== prevColCount );
        };

        $.Isotope.prototype._masonryGetContainerSize = function() {
            var unusedCols = 0,
                i = this.masonry.cols;
            // count unused columns
            while ( --i ) {
                if ( this.masonry.colYs[i] !== 0 ) {
                    break;
                }
                unusedCols++;
            }

            return {
                height : Math.max.apply( Math, this.masonry.colYs ),
                // fit container to columns that have been used;
                width : (this.masonry.cols - unusedCols) * this.masonry.columnWidth
            };
        };

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

