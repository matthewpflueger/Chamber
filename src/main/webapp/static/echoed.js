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
        var logout = new Echoed.Views.Components.Logout({el: '#logout', EvAg: EventAggregator});
        Backbone.history.start();
    }
};

Echoed.Models.Product = Backbone.Model.extend({
    initialize: function(){
    }
});

Echoed.Router = Backbone.Router.extend({
    initialize: function(options) {
        _.bindAll(this,'exhibit','friends','explore');
        this.EvAg = options.EvAg;
        this.page=null;
    },
    routes:{
        "_=_" : "fix",
        "": "explore",
        "explore": "explore",
        "explore/:filter": "explore",
        "exploref" : "exploreFriends",
        "exploref/:filter": "exploreFriends",
        "exhibit": "exhibit",
        "exhibit/:filter": "exhibit",
        "friends": "friends",
        "friends/exhibit/:id": "friendsExhibit",
        "friends/exhibit/:id/:filter": "friendsExhibit"
    },
    fix: function(){
        window.location.href = "#";
    },
    explore: function(filter){
        if(!filter) selector = "*";
        else selector = "." + filter;
        if(this.page != "Explore"){
            this.page = "Explore";
            pageView = new Echoed.Views.Pages.Exhibit({EvAg:this.EvAg, Filter: selector, Type: "explore"});
        }
        else{
            this.EvAg.trigger('filter/change',selector);
        }
        this.EvAg.trigger("page/change","explore");
    },
    exploreFriends: function(filter){
        if(!filter) selector = "*";
        else selector = "." + filter;
        if(this.page != "Explore/Friends"){
            this.page = "Explore/Friends";
            pageView = new Echoed.Views.Pages.Exhibit({EvAg:this.EvAg, Filter: selector, Type: "explore/friends"});
        }
        else
            this.EvAg.trigger('filter/change',selector);
        this.EvAg.trigger("page/change","explore");
    },
    exhibit: function(filter) {
        if(!filter) selector = "*";
        else selector = "." + filter;
        if(this.page != "Exhibit"){
            pageView = new Echoed.Views.Pages.Exhibit({EvAg: this.EvAg, Filter: selector, Type: "exhibit"});
            this.page = "Exhibit";
        } else{
            this.EvAg.trigger('filter/change',selector);
        }
        this.EvAg.trigger("page/change","exhibit");

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
                this.jsonUrl = "http://v1-api.echoed.com/user/exhibit/" + options.Id;
                this.baseUrl = "#friends/exhibit/" + options.Id + "/";
                this.contentTitle = "Friends Exhibit";
                this.id = "friends";
                break;
            case "explore":
                this.jsonUrl = "http://v1-api.echoed.com/user/feed/public";
                this.baseUrl = "#explore/";
                this.contentTitle = "Explore";
                this.feedSelector = "Everyone";
                this.id= "explore";
                break;
            case "explore/friends":
                this.jsonUrl = "http://v1-api.echoed.com/user/feed/friends";
                this.baseUrl = "#exploref/";
                this.contentTitle = "Explore";
                this.feedSelector = "Friends";
                this.id = "explore/friends";
                break;
            case "exhibit":
                this.jsonUrl = "http://v1-api.echoed.com/user/exhibit";
                this.baseUrl = "#exhibit/";
                this.contentTitle = "My Exhibit";
                this.id = null;
                break;
        }
        this.render();
    },
    render: function(){

        var self = this;

        $.ajax({
            url: self.jsonUrl,
            dataType: 'json',
            success: function(data){

                var template = _.template($('#templates-pages-exhibit').html());
                self.element.html(template);
                self.exhibit=$('#exhibit');


                var contentSelector = $('#content-selector');
                var ul = $('<ul></ul>').addClass('dropdown-container').appendTo(contentSelector);
                if(self.id == "explore" || self.id=="explore/friends"){
                    var viewDropDownEl = $('<li class="dropdown"></li>');
                    viewDropDownEl.appendTo(ul);
                    var viewDropDown = new Echoed.Views.Components.FeedDropdown({ el: viewDropDownEl, EvAg: self.EvAg, Id: self.id, BaseUrl: self.baseUrl, Selected: self.feedSelector});
                }
                var dropDownEl = $('<li class="dropdown"></li>');
                dropDownEl.appendTo(ul);
                var dropDown = new Echoed.Views.Components.Dropdown({el: dropDownEl, EvAg: self.EvAg, Id: self.id, BaseUrl: self.baseUrl, Filter: self.filter});
                var exhibit = $('#exhibit');


                var products = $('<div></div>');
                var echoes;
                if(self.id == "explore")
                    echoes = data;
                else
                    echoes = data.echoes;
                if(self.id == "friends")
                    $('#content-title').html(data.echoedUserName + "'s Exhibit");
                else
                    $('#content-title').html(self.contentTitle);
                if(echoes.length > 0){
                    exhibit.isotope({
                        animationOptions: {
                            duration: 500,
                            easing: 'linear',
                            queue: false
                        },
                        filter: self.filter
                    });
                    $.each(echoes, function(index,product){
                        var productModel = new Echoed.Models.Product(product);
                        self.addProduct(productModel,self.filter);
                        self.EvAg.trigger('category/add',product.echoCategory,product.echoCategory);
                    });
                }
                else{
                    var noEchoDiv = $('<div></div>').addClass("no-echoes").html("There are currently no echoes.");
                    noEchoDiv.appendTo(exhibit);
                }
            },
            error:function (xhr, ajaxOptions, thrownError){
            }
        });
    },
    filterProducts: function(selector){
        var self = this;
        self.exhibit.isotope({filter: '#exhibit .item_wrap' + selector});
    },
    addProduct: function(productModel,filter){
        var self = this;
        var productDiv = $('<div></div>');
        var productComponent = new Echoed.Views.Components.Product({el:productDiv, model:productModel});
        productDiv.hide().appendTo(self.exhibit);
        self.exhibit.imagesLoaded(function(){
            self.exhibit.isotope('insert',productDiv);
            productDiv.show();
        });
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
        var template = _.template($('#templates-pages-exhibit').html());
        this.element.html(template);
        var ex = $('#exhibit');
        jsonUrl = "http://v1-api.echoed.com/user/friends";
        $.ajax({
            url: jsonUrl,
            dataType: 'json',
            success: function(data){
                if(data.length > 0 ){
                    $.each(data, function(index,friend){
                        var img = "";
                        var friendImage = $('<div class="friend-img"></div>');
                        var friendText = $('<div class="friend-text"></div>').html(friend.name);
                        var  a = $('<a></a>').attr("href","#friends/exhibit/" + friend.toEchoedUserId);
                        if(friend.facebookId != null)
                            img = $('<img />').attr("height","50px").attr("src","http://graph.facebook.com/" + friend.facebookId + "/picture");
                        else
                            img = $('<img />').attr("src", "http://api.twitter.com/1/users/profile_image/" + friend.twitterId);
                        friendImage.append(img);
                        var friendDiv = $('<div></div>').addClass("friend").append(friendImage).append(friendText).appendTo(a);
                        a.appendTo(ex);
                    });
                }
                else{
                    var noEchoDiv = $('<div></div>').addClass("no-echoes").html("You have no friends on Echoed. Get your friends to join!");
                    noEchoDiv.appendTo(ex);
                }
            },
            error:function (xhr, ajaxOptions, thrownError){
            }
        });
    }
});

Echoed.Views.Components.Logout = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'triggerClick');
        this.EvAg = options.EvAg;
        this.el = options.el;
        this.element = $(this.el);
        //this.render();
    },
    events:{
        "click": "triggerClick"
    },
    triggerClick: function(){
    }
});

Echoed.Views.Components.FeedDropdown = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.baseUrl = options.BaseUrl;
        this.el = options.el
        this.element = $(this.el);
        if(options.Selected == "")
            options.Selected = "Everyone";
        this.selected = options.Selected;
        this.render();
    },
    events: {
        "mouseenter": "showList",
        "mouseleave": "hideList"
    },
    render: function(){
        this.element.html("<strong>View </strong>: " + this.selected + '<div></div>');
        var ul = $('<ul></ul>');
        ul.appendTo(this.element);
        this.dropDown = ul;
        var li = $('<li></li>').appendTo(ul);
        var anc =$('<a></a>').attr("href", "#explore").html("Everyone").appendTo(li);
        if(this.selected == "Everyone")
            anc.addClass("current");
        anc =$('<a></a>').attr("href", "#exploref").html("Friends").appendTo(li);
        if(this.selected == "Friends")
            anc.addClass("current");
    },
    showList: function(){
        this.dropDown.show();
    },
    hideList: function(){
        this.dropDown.hide();
    },
    triggerClick: function(e){
        this.render();
    }
});

Echoed.Views.Components.Dropdown = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'addSelector','triggerClick');
        this.EvAg = options.EvAg;
        this.categories = new Array();
        this.EvAg.bind('category/add', this.addSelector);
        this.EvAg.bind('filter/change',this.triggerClick);
        this.baseUrl = options.BaseUrl;
        this.el = options.el;
        this.element = $(this.el);
        this.selected = options.Filter.substr(1);
        if(this.selected == "")
            this.selected = "All";
        this.render();
    },
    events: {
        "mouseenter": "showList",
        "mouseleave": "hideList"
    },
    render: function(){
        this.element.html('<strong>Category : </strong>' + this.selected + "<div></div>");
        var sortedKeys = new Array();
        var sortedObj = {};
        for(var i in this.categories){
            sortedKeys.push(i);
        }
        sortedKeys.sort();
        for(var j in sortedKeys){
            sortedObj[sortedKeys[j]] = this.categories[sortedKeys[j]];
        }
        this.categories = sortedObj;

        var ul = $('<ul></ul>');
        ul.appendTo(this.element);
        this.dropDown = ul;
        var li = $('<li></li>').appendTo(ul);
        var anc =$('<a></a>').attr("id","All").attr("href", this.baseUrl).html("All").appendTo(li);
        if(this.selected == "All")
            anc.addClass("current");
        for(var id in this.categories){
            anc = $('<a></a>').attr("id",id).attr("href", this.baseUrl + this.categories[id].replace(" ","-")).html(id).appendTo(li);
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
    showList: function(){
        this.dropDown.show();
    },
    hideList: function(){
        this.dropDown.hide();
    },
    triggerClick: function(e){
        if(e == "*")
            e = ".All";
        this.selected = e.substr(1);
        this.render();
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
        "mouseenter": "showList",
        "mouseleave": "hideList"
    },
    render: function(){

    },
    showList: function(){

    },
    hideList: function(){

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
        var self = this;
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
            hover.append('<span class="highlight"><strong>' + this.model.get("echoedUserName") + '</strong></span><br/><br/>');
        if(this.model.get("echoCredit")){
            hover.append("<span class='highlight'><strong>Reward: $" + this.model.get("echoCredit").toFixed(2) +'</strong></span><br/><br/>');
            self.showOverlay();
            var t = setTimeout(self.hideOverlay, 3000);
        }
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
