define(
    'components/infiniteScroll',
    ['jquery', 'backbone', 'underscore'],
    function($, Backbone, _){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.EvAg = options.EvAg;
                this.EvAg.bind("triggerInfiniteScroll", this.triggerScroll);
                this.EvAg.bind("infiniteScroll/lock", this.lock);
                this.EvAg.bind("infiniteScroll/unlock", this.unlock);
                this.EvAg.bind("fade/show", this.off);
                this.EvAg.bind("fade/hide", this.on);
                this.EvAg.bind("infiniteScroll/on", this.on);
                this.EvAg.bind("infiniteScroll/off", this.off);
                this.element = $(options.el);
                this.locked = false;
                var self = this;
                $(window).scroll(function(){
                    if($(window).scrollTop() + 600 >= $(document).height() - $(window).height() && self.locked == false && self.status == true){
                        self.EvAg.trigger("infiniteScroll");
                    }
                });
            },
            on: function(){
                this.status = true;
            },
            off: function(){
                this.status = false;
            },
            lock: function(){
                this.element.show();
                this.locked = true
            },
            unlock: function(){
                var self = this;
                self.element.hide();
                self.locked = false;
                if($(window).scrollTop() + 600 >= $(document).height() - $(window).height() && self.locked == false && self.status == true){
                    self.EvAg.trigger("infiniteScroll");
                }
            },
            triggerScroll: function(){
                var noVScroll = $(document).height() <= $(window).height();
                if(noVScroll){
                    this.EvAg.trigger('infiniteScroll');
                }
            }
        });
    }
);
