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
        'models/user'
    ],
    function(require, $, _, Backbone, isotope, Router, Exhibit, Story, PageTitle, Title, Input, User, MessageHandler, Notifications, ErrorLog, Actions, InfiniteScroll, Nav, Login, ModelUser){

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
            cols = Math.max( cols, 3 );

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
                exhibitShowLogin: true
            };

            //Initialize Models
            this.modelUser = new ModelUser({ echoedUser: Echoed.echoedUser });

            //Options
            this.options = function(el){
                return {
                    el: el,
                    properties: this.properties,
                    modelUser: this.modelUser,
                    EvAg: this.EventAggregator
                }
            };





            this.errorLog = new ErrorLog({ EvAg: EventAggregator, properties: properties, modelUser: this.modelUser });


            this.router = new Router({ EvAg: EventAggregator, properties: properties, modelUser: this.modelUser });
            this.infiniteScroll = new InfiniteScroll({ el: '#infiniteScroll', EvAg: EventAggregator, properties: properties, modelUser: this.modelUser});
            this.nav = new Nav({ EvAg: EventAggregator, properties: properties, modelUser: this.modelUser});
            this.story = new Story({ el: '#story-container', EvAg: EventAggregator, properties: properties, modelUser: this.modelUser });
            this.exhibit = new Exhibit({ el: '#content', EvAg: EventAggregator, properties: properties, modelUser: this.modelUser });
            this.pageTitle = new PageTitle({ el: 'title', EvAg: EventAggregator, properties: properties, modelUser: this.modelUser });
            this.contentTitle = new Title({ el: '#title-container', EvAg: EventAggregator, properties: properties, modelUser: this.modelUser });
            this.actions = new Actions({ el: '#actions', EvAg: EventAggregator, properties: properties, modelUser: this.modelUser });
            this.input = new Input({ el: '#field-container', EvAg: EventAggregator, properties: properties });
            this.user = new User({ el: '#user', EvAg: EventAggregator, properties: properties });
            this.notifications = new Notifications({ el: '#notifications-container', EvAg: EventAggregator, properties: properties });
            this.login = new Login({ el: '#login-container', EvAg: EventAggregator, properties: properties });


            var iFrameNode = document.createElement('iframe');

            iFrameNode.height = '0px';
            iFrameNode.width = '0px';
            iFrameNode.style.border = "none";
            iFrameNode.id = "echoed-iframe";
            iFrameNode.src = Echoed.urls.api.replace("http") + "/echo/iframe";
            document.getElementsByTagName('body')[0].appendChild(iFrameNode);
            this.messageHandler = new MessageHandler({ el: '#echoed-iframe', EvAg: EventAggregator, properties: properties });

            Backbone.history.start();
            }
        );
    }
);

