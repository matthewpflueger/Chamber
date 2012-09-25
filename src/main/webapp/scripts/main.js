require.config({
    paths: {
        'backbone': 'libs/backbone-amd-0.9.2-min',
        'underscore': 'libs/underscore-amd-1.3.3-min',
        'jquery': 'libs/jquery-1.8.1.min',
        'isotope': 'libs/jquery.isotope.min',
        'expanding' : 'libs/expanding',
        'fileuploader': 'libs/fileuploader',
        'text': 'libs/require/text',
        'requireLib': 'libs/require/require'
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
        'underscore',
        'backbone',
        'isotope',
        'routers/website',
        'components/exhibit',
        'components/story',
        'components/fade',
        'components/pageTitle',
        'components/title',
        'components/input',
        'components/login',
        'components/messageHandler',
        'components/notifications',
        'components/errorLog',
        'components/actions',
        'components/categoryList',
        'components/menu',
        'components/infiniteScroll',
        'components/nav'
    ],
    function(require, $, _, Backbone, isotope, Router, Exhibit, Story, Fade, PageTitle, Title, Input, Login, MessageHandler, Notifications, ErrorLog, Actions, CategoryList, Menu, InfiniteScroll, Nav){

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
                width: Math.max(cols * colW - 10, 960)
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
            var EventAggregator = _.extend({}, Backbone.Events);

            var properties = {
                urls: Echoed.urls,
                echoedUser: Echoed.echoedUser,
                exhibitShowLogin: true
            };


            this.errorLog = new ErrorLog({ EvAg: EventAggregator, properties: properties });

            this.router = new Router({ EvAg: EventAggregator, properties: properties });
            this.infiniteScroll = new InfiniteScroll({ el: '#infiniteScroll', EvAg: EventAggregator, properties: properties})
            this.nav = new Nav({ EvAg: EventAggregator, properties: properties});
            this.fade = new Fade({ el: '#fade', EvAg: EventAggregator, properties: properties });
            this.story = new Story({ el: '#story', EvAg: EventAggregator, properties: properties });
            this.exhibit = new Exhibit({ el: '#content', EvAg: EventAggregator, properties: properties });
            this.pageTitle = new PageTitle({ el: 'title', EvAg: EventAggregator, properties: properties });
            this.contentTitle = new Title({ el: '#title-container', EvAg: EventAggregator, properties: properties });
            this.actions = new Actions({ el: '#actions', EvAg: EventAggregator, properties: properties });
            this.input = new Input({ el: '#field', EvAg: EventAggregator, properties: properties });
            this.login = new Login({ el: '#user', EvAg: EventAggregator, properties: properties });
            this.notifications = new Notifications({ el: '#notifications-container', EvAg: EventAggregator, properties: properties });
            this.menu = new Menu({ el: "#menu", EvAg: EventAggregator, properties: properties });
            this.categoryList = new CategoryList({ el: '#category-nav', EvAg: EventAggregator, properties: properties});

            var iFrameNode = document.createElement('iframe');

            iFrameNode.height = '0px';
            iFrameNode.width = '0px';
            iFrameNode.style.border = "none";
            iFrameNode.id = "echoed-iframe";
            iFrameNode.src = Echoed.urls.api + "/echo/iframe";
            document.getElementsByTagName('body')[0].appendChild(iFrameNode);
            this.messageHandler = new MessageHandler({ el: '#echoed-iframe', EvAg: EventAggregator, properties: properties });

            Backbone.history.start();
            }
        );
    }
);

