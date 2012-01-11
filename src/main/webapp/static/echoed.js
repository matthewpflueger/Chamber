
var EventAggregator = _.extend({}, Backbone.Events);

Echoed = {
    Views: {
        Pages:{},
        Components:{}
    },
    Models:{},
    Collections:{},
    init: function() {
        var router = new Echoed.Router({EvAg: EventAggregator});
        var nav = new Echoed.Views.Components.Nav({EvAg: EventAggregator});
        Backbone.history.start();
    }
};

Echoed.Models.Product = Backbone.Model.extend({
    initialize: function(){
    }
});

Echoed.Router = Backbone.Router.extend({
    initialize: function(options) {
        _.bindAll(this,'exhibit','friends');
        this.EvAg = options.EvAg;
        this.page = null;

    },
    routes:{
        "_=_" : "fix",
        "": "feed",
        "feed": "feed",
        "feed/:filter": "feed",
        "exhibit": "exhibit",
        "exhibit/:filter": "exhibit",
        "friends": "friends",
        "friends/exhibit/:id": "friendsExhibit",
        "friends/exhibit/:id/:filter": "friendsExhibit"
    },
    fix: function(){
        window.location.href = "#";
    },
    feed: function(filter){
        if(!filter) selector = "*";
        else selector = "." + filter;
        if(this.page != "Feed")
            pageView = new Echoed.Views.Pages.Exhibit({EvAg:this.EvAg, Filter: selector, Type: "feed"});
        this.EvAg.trigger('filter/change',selector);
        this.EvAg.trigger("page/change","feed");
        this.page= "Feed";
    },
    exhibit: function(filter) {
        if(!filter) selector = "*";
        else selector = "." + filter;

        if(this.page != "Exhibit")
            pageView = new Echoed.Views.Pages.Exhibit({EvAg: this.EvAg, Filter: selector, Type: "exhibit"});

        this.EvAg.trigger('filter/change',selector);
        this.EvAg.trigger("page/change","exhibit");
        this.page = "Exhibit";
    },
    friends: function() {
        pageView = new Echoed.Views.Pages.Friends({EvAg: this.EvAg});
        this.EvAg.trigger("page/change","friends");
        this.page = "Friends";
    },
    friendsExhibit: function(id, filter){
        if(!filter) selector = "*";
        else selector = "." + filter;
        var newPage = "Friends/Exhibit/" + id
        if(this.page != newPage)
            pageView = new Echoed.Views.Pages.Exhibit({EvAg: this.EvAg, Filter: selector, Type: "friend", Id: id});
        this.EvAg.trigger("filter/change",selector);
        this.EvAg.trigger("page/change","friends");
        this.page = newPage
    }

});

Echoed.Views.Pages.Exhibit = Backbone.View.extend({
    el: '#content',
    initialize: function(options){

        _.bindAll(this,'render','addProduct','filterProducts');
        this.EvAg = options.EvAg;
        this.EvAg.bind('products/add', this.addProduct);
        this.EvAg.bind('filter/change', this.filterProducts);
        this.element = $(this.el);
        this.filter = options.Filter;
        switch(options.Type){
            case "friend":
                this.jsonUrl = "http://v1-api.echoed.com/closet/exhibit/" + options.Id;
                this.baseUrl = "#friends/exhibit/" + options.Id + "/";
                this.id = options.Id;
                break;
            case "feed":
                this.jsonUrl = "http://v1-api.echoed.com/closet/feed";
                this.baseUrl = "#feed/";
                this.contentTitle = "My Feed";
                this.id= "feed";
                break;
            case "exhibit":
                this.jsonUrl = "http://v1-api.echoed.com/closet/exhibit";
                this.baseUrl = "#exhibit/";
                this.contentTitle = "My Exhibit";
                this.id = null;
                break;
        }
        this.render();
    },
    render: function(){
        var template = _.template($('#templates-pages-exhibit').html());
        this.element.html(template);
        $('#content-title').html(this.contentTitle);
        var self = this;
        var productSelector = new Echoed.Views.Components.ProductSelector({EvAg: this.EvAg, Id:this.id,BaseUrl: this.baseUrl});

        var exhibit = $('#exhibit');

        exhibit.isotope({
            filter: self.filter
        });

        $.ajax({
            url: self.jsonUrl,
            dataType: 'json',
            success: function(data){
                var products = $('');
                $.each(data, function(index,product){
                    var productModel = new Echoed.Models.Product(product);
                    var productDiv = $('<div></div>');
                    var productComponent = new Echoed.Views.Components.Product({el:productDiv, model:productModel});
                    products.after(productDiv);

                    self.addProduct(productModel,self.filter);
                    self.EvAg.trigger('category/add',product.echoCategory,product.echoCategory);
                });
                products.appendTo(exhibit);
                exhibit.imagesLoaded(function(){
                    exhibit.isotope('insert',products);
                });
           },
            error:function (xhr, ajaxOptions, thrownError){
            }
        });
    },
    filterProducts: function(selector){
        $('#exhibit').isotope({filter: '#exhibit .item_wrap' + selector});
    },
    addProduct: function(productModel,filter){
        var exhibit = $('#exhibit');
        var productDiv = $('<div></div>');
        var productComponent = new Echoed.Views.Components.Product({el: productDiv, model:productModel});
        productDiv.hide().appendTo(exhibit);
        exhibit.imagesLoaded(function(){
            exhibit.isotope('insert',productDiv);
            productDiv.show();
        });
        return productDiv;
    }
});

Echoed.Views.Pages.Friends = Backbone.View.extend({
    el: '#content',
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.render();
    },
    render: function(){
        var template = _.template($('#templates-pages-friends').html());
        this.element.html(template);
        var ex = $('#friends');
        jsonUrl = "http://v1-api.echoed.com/closet/friends";
        $.ajax({
            url: jsonUrl,
            dataType: 'json',
            success: function(data){
                $.each(data, function(index,friend){
                    var img = "";
                    var friendImage = $('<div class="friend-img"></div>');
                    var friendText = $('<div class="friend-text"></div>').html(friend.name);
                    var  a = $('<a></a>').attr("href","#friends/exhibit/" + friend.toEchoedUserId);
                    if(friend.facebookId != null)
                        img = $('<img />').attr("src","http://graph.facebook.com/" + friend.facebookId + "/picture");
                    else
                        img = $('<img />').attr("src", "http://api.twitter.com/1/users/profile_image/" + friend.twitterId);
                    friendImage.append(img);
                    var friendDiv = $('<div></div>').addClass("friend").append(friendImage).append(friendText).appendTo(a);
                    a.appendTo(ex);

                });
            },
            error:function (xhr, ajaxOptions, thrownError){
            }
        });
    }
});

Echoed.Views.Components.ProductSelector = Backbone.View.extend({
    el: '#content-selector',
    initialize: function(options){
        _.bindAll(this,'addSelector','triggerClick');
        this.EvAg = options.EvAg;
        this.EvAg.bind('category/add',this.addSelector);
        this.EvAg.bind('filter/change',this.triggerClick);
        this.categories = new Array();
        this.element = $(this.el);
        this.baseUrl = options.BaseUrl;
        this.render();
    },
    render: function(){
        //this.categories.sort();
        var sortedKeys = new Array();
        var sortedObj = {};
        for(var i in this.categories){
            sortedKeys.push(i)
        }
        sortedKeys.sort();
        for(var j in sortedKeys){
            sortedObj[sortedKeys[j]] = this.categories[sortedKeys[j]];
        }
        this.categories = sortedObj;

        this.element.html('')
        $('<a></a>').attr("id","All").attr("href", this.baseUrl).html("All").appendTo(this.element);
        for(var id in this.categories){
            var anc = $('<a></a>').attr("id",id).attr("href", this.baseUrl + this.categories[id].replace(" ","-")).html(id).appendTo(this.element);
            if(id == this.selected){
                anc.addClass("current");
            }
        }
    },
    addSelector: function(selector,text){
        if(!this.categories[text]){
            this.categories[text] =  selector;
            this.render();
        }
    },
    triggerClick: function(e){
        this.element.find(".current").removeClass("current");
        $("#" + e.substr(1)).addClass("current");
        this.selected = e.substr(1);
    }
});

Echoed.Views.Components.Nav = Backbone.View.extend({
    el: "#header-nav",
    initialize: function(options){
        _.bindAll(this);
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind("page/change",this.highlight);
        this.render();
    },
    render: function(){

    },
    highlight: function(page){
        this.element.find(".current").removeClass("current");
        $("#" + page + "_nav").addClass("current");
    }
});

Echoed.Views.Components.Tooltip = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.el = options.el;
        this.element = $(this.el);
        this.posX = this.element.offset.left;
        this.posY = this.element.offset.bottom;
        this.render();
    },
    events:{

    },
    render: function(){

    }
});

Echoed.Views.Components.Product = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'showOverlay','hideOverlay');
        this.el = options.el;
        this.render();
    },
    events:{
        "mouseenter": "showOverlay",
        "mouseleave": "hideOverlay",
        "click": "click"
    },
    render: function(){
        var template = _.template($('#templates-components-product').html());

        this.el.addClass("item_wrap").addClass(this.model.get("echoCategory").replace(" ","-")).html(template).attr("href","http://v1-api.echoed.com/echo/" + this.model.get("echoId") + "/1");
        var hover = this.el.find(".item_hover_wrap");
        var img = this.el.find("img");
        if(this.model.get("echoProductName"))
            hover.append('<strong>' + this.model.get("echoProductName") +'</strong><br/><br/>');
        if(this.model.get("echoBrand"))
            hover.append('<strong>by ' + this.model.get("echoBrand") + '</strong><br/><br/>');
        if(this.model.get("retailerName"))
            hover.append('@ ' + this.model.get("retailerName") + '<br/><br/>');
        if(this.model.get("echoedUserName"))
            hover.append(this.model.get("echoedUserName") + '<br/><br/>');
        if(this.model.get("echoCredit"))
            hover.append("My Reward: $" + this.model.get("echoCredit").toFixed(2) +'<br/><br/>');
        img.attr('src',this.model.get("echoImageUrl"));
        return this;
    },
    showOverlay: function(){
        this.el.find('.item_hover').fadeIn('fast');
    },
    hideOverlay: function(){
        this.el.find('.item_hover').fadeOut('fast');
    },
    click: function(){
        var url = this.el.attr("href");
        window.open(url);
    }
});
