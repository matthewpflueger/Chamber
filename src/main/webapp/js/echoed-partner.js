if (!('Echoed' in this)) this.Echoed = {};

$(document).ready(function() {
    Echoed.init();
});

var EventAggregator = _.extend({}, Backbone.Events);

Echoed = {
    Views: {
        Pages:{},
        Components:{}
    },
    Models:{},
    init: function() {
        var router = new Echoed.Router({EvAg: EventAggregator});
        var page = new Echoed.Pages.Partner({ el: '#content', EvAg: EventAggregator });
        Backbone.history.start();
    }
};

Echoed.Router = Backbone.Router.extend({
    routes: {
        '': "home"
    },
    home: function(){
    }
});

Echoed.Pages.Partner = Backbone.View.extend({
    initialize: function(options){
        this.element = $(options.el);
        this.EvAg = options.EvAg;
    }
});