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
        var infiniteScroll = new Echoed.Views.Components.InfiniteScroll({EvAg : EventAggregator});
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
        "explore/": "explore",
        "explore/:filter": "explore",
        "exploref/": "exploreFriends",
        "exploref" : "exploreFriends",
        "exploref/:filter": "exploreFriends",
        "exhibit/": "exhibit",
        "exhibit": "exhibit",
        "exhibit/:filter": "exhibit",
        "friends": "friends",
        "friends/exhibit/:id": "friendsExhibit",
        "friends/exhibit/:id/": "friendsExhibit",
        "friends/exhibit/:id/:filter": "friendsExhibit",
        "partners/:name/:filter": "partnerFeed",
        "partners/:name/": "partnerFeed",
        "partners/:name": "partnerFeed"
    },
    fix: function(){
        window.location.href = "#";
    },
    explore: function(filter){
        if(this.page != "Explore"){
            this.page = "Explore";
            pageView = new Echoed.Views.Pages.Exhibit({EvAg:this.EvAg, Filter: filter, Type: "explore"});
        }
        else{
            this.EvAg.trigger('filter/change',filter);
        }
        this.EvAg.trigger("page/change","explore");
    },
    exploreFriends: function(filter){
        if(this.page != "Explore/Friends"){
            this.page = "Explore/Friends";
            pageView = new Echoed.Views.Pages.Exhibit({EvAg:this.EvAg, Filter: filter, Type: "explore/friends"});
        }
        else
            this.EvAg.trigger('filter/change',filter);
        this.EvAg.trigger("page/change","explore");
    },
    partnerFeed: function(name,filter) {
        var newPage = "Partner/" + name;
        if(this.page != newPage){
            pageView = new Echoed.Views.Pages.Exhibit({EvAg:this.EvAg, Filter: filter, Type: "partners", Name: name});
            this.page = newPage
        } else{
            this.EvAg.trigger("filter/change", filter);
        }
        this.EvAg.trigger("page/change","partners");
    },
    exhibit: function(filter) {
        if(this.page != "Exhibit"){
            pageView = new Echoed.Views.Pages.Exhibit({EvAg: this.EvAg, Filter: filter, Type: "exhibit"});
            this.page = "Exhibit";
        } else{
            this.EvAg.trigger('filter/change',filter);
        }
        this.EvAg.trigger("page/change","exhibit");

    },
    friends: function() {
        pageView = new Echoed.Views.Pages.Friends({EvAg: this.EvAg});
        this.EvAg.trigger("page/change","friends");
        this.page = "Friends";
    },
    friendsExhibit: function(id, filter){
        var newPage = "Friends/Exhibit/" + id;
        if(this.page != newPage){
            pageView = new Echoed.Views.Pages.Exhibit({EvAg: this.EvAg, Filter: filter, Type: "friend", Id: id});
            this.page = newPage
        } else {
            this.EvAg.trigger("filter/change",filter);
        }
        this.EvAg.trigger("page/change","friends");
    }

});

Echoed.Views.Components.InfiniteScroll = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'triggerScroll');
        this.EvAg = options.EvAg;
        this.EvAg.bind("triggerInfiniteScroll",this.triggerScroll);
        var self = this;
        $(window).scroll(function(){
            if($(window).scrollTop() +50 >= $(document).height() - $(window).height()){
                self.EvAg.trigger("infiniteScroll");
            }
        });
    },
    triggerScroll: function(){
        var self = this;
        var noVScroll = $(document).height() <= $(window).height();
        if(noVScroll){
            self.EvAg.trigger('infiniteScroll');
        }
    }

});

Echoed.Views.Pages.Exhibit = Backbone.View.extend({
    el: '#content',
    initialize: function(options){
        _.bindAll(this,'render','addProduct','filterProducts','next','complete','relayout');
        this.EvAg = options.EvAg;
        this.EvAg.bind('products/add', this.addProduct);
        this.EvAg.bind('filter/change', this.filterProducts);
        this.EvAg.bind('exhibit/relayout', this.relayout);
        this.productCount = 0;
        this.productCount2 = 0;
        this.element = $(this.el);
        if(!options.Filter)
            this.filter = '*';
        else
            this.filter = "." + options.Filter;

        switch(options.Type){
            case "friend":
                this.jsonUrl = Echoed.urls.api + "/user/exhibit/" + options.Id;
                this.baseUrl = "#friends/exhibit/" + options.Id + "/";
                this.contentTitle = "Your Friends";
                this.id = "friends";
                this.nextInt = 1;
                break;
            case "partners":
                this.jsonUrl = Echoed.urls.api + "/user/feed/partner/" + options.Name;
                this.baseUrl = "#partners/" + options.Name + "/";
                this.contentTitle = options.Name;
                this.id = "partners";
                this.nextInt = 1;
                break;
            case "explore":
                this.jsonUrl = Echoed.urls.api + "/user/feed/public";
                this.baseUrl = "#explore/";
                this.contentTitle = "What People Are Buying";
                this.feedSelector = "Everyone";
                this.id= "explore";
                this.nextInt = 1;
                break;
            case "explore/friends":
                this.jsonUrl = Echoed.urls.api + "/user/feed/friends";
                this.baseUrl = "#exploref/";
                this.contentTitle = "What Your Friends Are Sharing";
                this.feedSelector = "Friends";
                this.id = "explore/friends";
                this.nextInt = 1;
                break;
            case "exhibit":
                this.jsonUrl = Echoed.urls.api + "/user/exhibit";
                this.baseUrl = "#exhibit/";
                this.contentTitle = "My Exhibit";
                this.id = null;
                this.nextInt = 1;
                break;
        }
        this.render();
    },
    render: function(){

        var self = this;

        $.ajax({
            url: self.jsonUrl,
            xhrFields: {
                withCredentials: true
            },
            dataType: 'json',
            success: function(data){

                var template = _.template($('#templates-pages-exhibit').html());
                self.element.html(template);
                self.exhibit=$('#exhibit');

                var contentSelector = $('#content-selector');
                contentSelector.html('');
                var ul = $('<ul></ul>').addClass('dropdown-container').appendTo(contentSelector);
                if(self.id == "explore" || self.id=="explore/friends"){
                    var viewDropDownEl = $('<li class="dropdown"></li>');
                    viewDropDownEl.appendTo(ul);
                    var viewDropDown = new Echoed.Views.Components.FeedDropdown({ el: viewDropDownEl, EvAg: self.EvAg, Id: self.id, BaseUrl: self.baseUrl, Selected: self.feedSelector});
                }
                var dropDownEl = $('<li class="dropdown"></li>');
                dropDownEl.appendTo(ul);
                var brandDropDownEl = $('<li class="dropdown"></li>');
                brandDropDownEl.appendTo(ul);
                var categoryDropDown = new Echoed.Views.Components.Dropdown({el: dropDownEl,Name: 'Category', EvAg: self.EvAg, Id: self.id, BaseUrl: self.baseUrl, Filter: self.filter});
                //var brandDropdown = new Echoed.Views.Components.Dropdown({ el: brandDropDownEl, Name: 'Brand', EvAg: self.EvAg, Id: self.id, BaseUrl: self.baseUrl, Filter: self.filter});
                var exhibit = $('#exhibit');
                if(self.id == "friends")
                    $('#content-title').html(data.echoedUserName + "'s Exhibit");
                else
                    $('#content-title').html(self.contentTitle);
                if(data.echoes.length > 0){
                    exhibit.isotope({
                        masonry:{
                            columnWidth: 5
                        },
                        itemSelector: '.item_wrap',
                        filter: self.filter
                    });
                    $.each(data.echoes, function(index,product){
                        var productModel = new Echoed.Models.Product(product);
                        self.addProduct(productModel,self.filter);
                        self.EvAg.trigger('Category/add',product.echoCategory,product.echoCategory);
                        self.EvAg.trigger('Brand/add',product.echoBrand, product.echoBrand);
                    });
                    self.productCount += data.echoes.length;
                    self.EvAg.bind('infiniteScroll', self.next);
                }
                else{
                    self.nextInt = null;
                    var noEchoDiv = $('<div></div>').addClass("no-echoes").html("There are currently no Echoes.");
                    noEchoDiv.appendTo(exhibit);
                }
            }
        });

    },
    relayout: function(e){
        var self = this;
        self.exhibit.isotope('reLayout', function(){
            $("html, body").animate({scrollTop: e.offset().top - 90 }, 300);
        });
    },
    filterProducts: function(filter){
        var self = this;

        if(!filter)
            selector = '*';
        else
            selector = "." + encodeURIComponent(filter);
        self.exhibit.isotope({filter: '#exhibit .item_wrap' + selector});
    },
    next: function(){
        var self = this;
        if(self.nextInt != null){
            self.EvAg.unbind('infiniteScroll');
            $.ajax({
                url: self.jsonUrl + "?page=" + self.nextInt,
                xhrFields: {
                    withCredentials: true
                },
                dataType: 'json',
                success: function(data){
                    if(data.echoes.length > 0){
                        $.each(data.echoes, function(index,product){
                            var productModel = new Echoed.Models.Product(product);
                            self.addProduct(productModel,self.filter);
                            self.EvAg.trigger('Category/add',product.echoCategory,product.echoCategory);
                            self.EvAg.trigger('Brand/add',product.echoBrand,product.echoBrand);
                        });
                        self.productCount += data.echoes.length;
                        self.EvAg.bind('infiniteScroll', self.next);
                        self.nextInt++;

                    }
                    else{
                        self.nextInt = null;
                    }
                }
            });
        }
    },
    complete: function(){
        var self = this;
        self.EvAg.bind('infiniteScroll', self.next);
    },
    addProduct: function(productModel,filter){
        var self = this;
        var productDiv = $('<div></div>');
        var productComponent = new Echoed.Views.Components.Product({el:productDiv, model:productModel, EvAg: self.EvAg });
        var imageHeight = productModel.get("image").preferredHeight;
        if(imageHeight) {
            self.exhibit.isotope('insert',productDiv);
        } else {
            productDiv.imagesLoaded(function(){
                self.exhibit.isotope('insert',productDiv);
            });
        }
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
        jsonUrl = Echoed.urls.api + "/user/friends";
        $.ajax({
            url: jsonUrl,
            xhrFields: {
                withCredentials: true
            },
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

        this.name = options.Name;

        this.EvAg = options.EvAg;
        this.categories = new Array();
        this.EvAg.bind(this.name + '/add', this.addSelector);
        this.EvAg.bind('filter/change',this.triggerClick);
        this.baseUrl = options.BaseUrl;
        this.el = options.el;
        this.element = $(this.el);
        this.triggerClick(options.Filter.substr(1));
        this.render();
    },
    events: {
        "mouseenter": "showList",
        "mouseleave": "hideList"
    },
    render: function(){
        this.element.html('<strong>' + this.name + ': </strong>' + this.selected + "<div></div>");
        var sortedKeys = new Array();
        var sortedObj = {};
        for(var i in this.list){
            sortedKeys.push(i);
        }
        sortedKeys.sort();
        for(var j in sortedKeys){
            sortedObj[sortedKeys[j]] = this.list[sortedKeys[j]];
        }
        this.list = sortedObj;

        var ul = $('<ul></ul>');
        ul.appendTo(this.element);
        this.dropDown = ul;
        var li = $('<li></li>').appendTo(ul);
        var anc =$('<a></a>').attr("id","All").attr("href", this.baseUrl).html("All").appendTo(li);
        if(this.selected == "All")
            anc.addClass("current");
        for(var id in this.list){
            anc = $('<a></a>').attr("id",id).attr("href", this.baseUrl + encodeURIComponent(this.name + "-" + id)).html(id).appendTo(li);
            if(id == this.selected){
                anc.addClass("current");
            }
        }
    },
    addSelector: function(selector,text){
        if(!this.list[text]){
            this.list[text] =  selector;
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
        this.selected = "All";
        if(e){
            var selectorArray = e.split("_");
            for(var i=0;i<selectorArray.length;i++){
                if(selectorArray[i].search(this.name,0)==0){
                    this.selected = decodeURIComponent(selectorArray[i].substr(this.name.length + 1));
                }
            }
        }
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
    render: function(){
        var template = _.template($('#templates-components-tooltip').html());
    },
    showTooltip: function(){
        this.el.fadeIn();
    },
    hideTooltip: function(){
        this.el.fadeOut();
    }
});

Echoed.Views.Components.Product = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'showOverlay','hideOverlay','enlarge','shrink','click');
        this.el = options.el;
        this.EvAg = options.EvAg;
        this.state =0;
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
        var landingUrl = Echoed.urls.api + "/echo/" + this.model.get("echoId");
        var imageUrl =   this.model.get("image").preferredUrl;
        var imageWidth = this.model.get("image").preferredWidth;
        var imageHeight = this.model.get("image").preferredHeight;
        this.el.addClass("item_wrap").addClass('Brand-' + encodeURIComponent(this.model.get("echoBrand"))).addClass('Category-' + encodeURIComponent(this.model.get("echoCategory"))).html(template).attr("href", landingUrl);
        var hover = this.el.find(".item_hover_wrap");
        var img = this.el.find("img");
        var text = this.el.find(".item_text");
        var tooltipEl = this.el.find(".product-tooltip");
        self.toolTip = new Echoed.Views.Components.Tooltip({ el: tooltipEl, EvAg: self.EvAg });
        if(this.model.get("echoProductName")){
            hover.append('<strong>' + this.model.get("echoProductName") +'</strong><br/><br/>');
            text.prepend(this.model.get("echoProductName")+'<br/>');
        }
        if(this.model.get("echoBrand"))
            hover.append('<strong>by ' + this.model.get("echoBrand") + '</strong><br/><br/>');

        if(this.model.get("retailerName")){
            text.prepend('<strong>' + this.model.get("retailerName") + '</strong><br/>');
            hover.append('@ ' + this.model.get("retailerName") + '<br/><br/>');
        }
        if(this.model.get("echoedUserName"))
            hover.append('<span class="highlight"><strong>' + this.model.get("echoedUserName") + '</strong></span><br/><br/>');
        if(this.model.get("echoCredit")){
            hover.append("<span class='highlight'><strong>Reward: $" + this.model.get("echoCredit").toFixed(2) +'</strong></span><br/><br/>');
        }
        img.attr('src', imageUrl);
        if (imageWidth > 0) {
            img.attr('width', imageWidth)
        }
        if (imageHeight > 0) {
            img.attr('height', imageHeight)
        }
        if(this.model.get("echoCreditWindowEndsAt")){
            var then = this.model.get("echoCreditWindowEndsAt");
            var a = new Date();
            var now = a.getTime();
            var diff = then - now;
            var daysleft = parseInt(diff/(24*60*60*1000));
            if(daysleft >= 0){
                hover.append("<span class='highlight'><strong>Days Left: "+ (daysleft + 1) + "</strong></span><br/><br/>");
                self.showOverlay();
                var t = setTimeout(self.hideOverlay, 3000);
                img.addClass("open-echo");
                var visits = this.model.get("echoTotalClicks");
                //hover.append("<span class='highlight'><strong>" + visits + " people have visited</strong></span>");
            }
        }
        return this;
    },
    showOverlay: function(){
        this.el.find('.item_hover').slideDown('fast');
        //this.el.css("width","460px");
        //this.EvAg.trigger('exhibit/relayout');
        //this.toolTip.showTooltip();
    },
    hideOverlay: function(){
        this.el.find('.item_hover').slideUp('fast');
        //this.el.find(".item_text").css("height","");
        //this.EvAg.trigger('exhibit/relayout');
        //this.el.find('.item_hover').fadeOut('fast');
        //this.el.css("width","");
        //this.EvAg.trigger('exhibit/relayout');
        //this.toolTip.hideTooltip();
    },
    enlarge: function(){
        var self = this;
        this.el.css("width","460px");
        this.EvAg.trigger("exhibit/relayout",self.el);
        this.state = 1;
    },
    shrink: function(){
        this.el.css("width","");
        this.EvAg.trigger("exhibit/relayout");
        this.state = 0;
    },
    click: function(){
        //if(this.state == 0){
            //this.enlarge();
        //} else {
          //  this.shrink();
        //}
        //this.EvAg.trigger('exhibit/relayout');
        var url = this.el.attr("href");
        window.open(url);
    }
});


function drawPinIt(c) {
    if (!(!c.className || c.className.indexOf("pin-it-button") < 0)) {
        var d = c.getAttribute("href");
        var b = {};
        d = d.slice(d.indexOf("?") + 1).split("&");
        for (var a = 0; a < d.length; a++) {
            var g = d[a].split("=");
            b[g[0]] = g[1]
        }
        b.layout = c.getAttribute("count-layout");
        b.count = c.getAttribute("always-show-count");
        a = "?";
        d = window.location.protocol + "//d3io1k5o0zdpqr.cloudfront.net/pinit.html";
        for (var f in b)if (b[f]) {
            d +=
                a + f + "=" + b[f];
            a = "&"
        }
        a = document.createElement("iframe");
        a.setAttribute("src", d);
        a.setAttribute("scrolling", "no");
        a.allowTransparency = true;
        a.frameBorder = 0;
        a.style.border = "none";
        if (b.layout == "none") {
            a.style.width = "43px";
            a.style.height = "20px"
        } else if (b.layout == "vertical") {
            a.style.width = "43px";
            a.style.height = "58px"
        } else {
            a.style.width = "90px";
            a.style.height = "20px"
        }
        c.parentNode.replaceChild(a, c)
    }
}