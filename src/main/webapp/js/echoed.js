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
        var infiniteScroll = new Echoed.Views.Components.InfiniteScroll({ el: '#infiniteScroll', EvAg : EventAggregator});
        //var actions = new Echoed.Views.Components.Actions({ el: '#actions', EvAg: EventAggregator });
        var filter = new Echoed.Views.Components.Dropdown({ el: '#content-selector', Name: 'Filter', EvAg: EventAggregator});
        var field = new Echoed.Views.Components.Field({ el: '#field', EvAg: EventAggregator });
        Backbone.history.start();
    }
};

Echoed.Models.Product = Backbone.Model.extend({
    initialize: function(){
    }
});

Echoed.Router = Backbone.Router.extend({
    initialize: function(options) {
        _.bindAll(this,'me','friends','explore');
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
        "echo/:partner/:product/:filter": "echo",
        "echo/:partner/:product": "echo",
        "me/friends": "friends",
        "me/": "me",
        "me": "me",
        "me/:filter": "me",
        "echo/:id": "echo",
        "friends/exhibit/:id": "friendsExhibit",
        "friends/exhibit/:id/": "friendsExhibit",
        "friends/exhibit/:id/:filter": "friendsExhibit",
        "partners/:name/:filter": "partnerFeed",
        "partners/:name/": "partnerFeed",
        "partners/:name": "partnerFeed"
    },
    echo: function(partner, product, filter) {
        if(this.page != "Echo"){
            this.page = "Echo";
            pageView = new Echoed.Views.Pages.Exhibit({EvAg: this.EvAg, Partner: partner, Product: product, Filter: filter, Type: "echo"});
        } else {
            this.EvAg.trigger("filter/change", filter);
        }
        this.EvAg.trigger("page/change","echo");
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
    me: function(filter) {
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
        this.EvAg.trigger('filter/hide');
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

Echoed.Views.Components.Field = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this, 'render', 'load', 'updateEndpoint','openPhotoDialog');
        this.element = $(options.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind("field/show", this.load);
        this.EvAg.bind("focus/update", this.updateEndpoint);
    },
    events: {
        "click .field-close": "close",
        "click .field-photo": "openPhotoDialog"
    },
    updateEndpoint: function(options){
        var self = this;
        self.endpointBase = Echoed.urls.api + '/api/partner/' + options.partner + '/' + options.product;
    },
    openPhotoDialog: function(options){
        var self = this;
    },
    load: function(action){
        var self = this;
        switch(action){
            case 'testimonial':
                self.type = action;
                self.template = _.template($('#templates-components-testimonial').html());
                self.endpoint = self.endpointBase + '/testimonial';
                self.render();
                break;
            case 'story':
                self.type = action;
                self.template = _.template($('#templates-components-story').html());
                self.endpoint = self.endpointBase + '/story';
                self.render();
                break;
            case 'ask':
                self.type = action;
                self.template = _.template($('#templates-components-ask').html());
                self.endpoint = self.endpointBase + '/ask';
                self.render();
                break;
            case 'photo':
                self.type = action;
                self.template = _.template($('#templates-components-photo').html());
                self.endpoint = self.endpointBase + '/photo';
                self.render();
                break;
        }
    },
    render: function(){
        var self = this;
        self.element.empty();
        self.element.html(self.template);
        self.element.fadeIn();
    },
    close: function(){
        var self = this;
        self.element.fadeOut();
        self.element.empty();
        self.element.fadeOut();
    }
});

Echoed.Views.Components.InfiniteScroll = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'triggerScroll', 'lock', 'unlock');
        this.EvAg = options.EvAg;
        this.EvAg.bind("triggerInfiniteScroll", this.triggerScroll);
        this.EvAg.bind("infiniteScroll/lock", this.lock);
        this.EvAg.bind("infiniteScroll/unlock", this.unlock);
        this.element = $(options.el);
        this.locked = false;
        var self = this;
        $(window).scroll(function(){
            if($(window).scrollTop() + 600 >= $(document).height() - $(window).height() && self.locked == false){
                self.EvAg.trigger("infiniteScroll");
            }
        });
    },
    lock: function(){
        var self = this;
        self.element.show();
        self.locked = true
    },
    unlock: function(){
        var self = this;
        self.element.hide();
        self.locked = false;
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
        _.bindAll(this,'render','addProduct','filterProducts','next','complete','relayout','addQuestion', 'addProducts','addTitle');
        this.EvAg = options.EvAg;
        this.EvAg.bind('products/add', this.addProduct);
        this.EvAg.bind('filter/change', this.filterProducts);
        this.EvAg.bind('exhibit/relayout', this.relayout);
        this.EvAg.bind('question/add', this.addQuestion);
        this.element = $(this.el);
        if(!options.Filter)
            this.filter = '*';
        else
            this.filter = "." + options.Filter;

        switch(options.Type){
            case "echo":
                this.jsonUrl = Echoed.urls.api + "/api/partner/" + options.Partner + "/" + options.Product;
                this.baseUrl = "#echo/" + options.Partner + "/" + options.Product;
                this.id = "echo";
                this.nextInt = 1;
                this.EvAg.trigger('focus/update', { partner: options.Partner , product: options.Product });
                break;
            case "friend":
                this.jsonUrl = Echoed.urls.api + "/api/user/" + options.Id;
                this.baseUrl = "#friends/exhibit/" + options.Id + "/";
                this.contentTitle = "Your Friends";
                this.id = "friends";
                this.nextInt = 1;
                break;
            case "partners":
                this.jsonUrl = Echoed.urls.api + "/api/partner/" + options.Name;
                this.baseUrl = "#partners/" + options.Name + "/";
                this.contentTitle = options.Name;
                this.id = "partners";
                this.nextInt = 1;
                break;
            case "explore":
                this.jsonUrl = Echoed.urls.api + "/api/me/feed";
                this.baseUrl = "#explore/";
                this.contentTitle = "Everyone";
                this.feedSelector = "Everyone";
                this.id= "explore";
                this.nextInt = 1;
                break;
            case "explore/friends":
                this.jsonUrl = Echoed.urls.api + "/api/me/feed/friends";
                this.baseUrl = "#exploref/";
                this.contentTitle = "What Your Friends Are Sharing";
                this.feedSelector = "Friends";
                this.id = "explore/friends";
                this.nextInt = 1;
                break;
            case "exhibit":
                this.jsonUrl = Echoed.urls.api + "/api/me/exhibit";
                this.baseUrl = "#me/";
                this.contentTitle = "Me";
                this.id = null;
                this.nextInt = 1;
                break;
        }
        this.EvAg.trigger('filter/init', this.baseUrl);
        var self = this;
        $.ajax({
            url: self.jsonUrl,
            xhrFields: {
                withCredentials: true
            },
            dataType: 'json',
            success: function(data){
                self.render(data);
            }
        });

    },
    render: function(data){
        var self = this;
        self.element.empty();
        if(typeof(self.exhibit) != "undefined") self.exhibit.isotope('destroy');
        var template = _.template($('#templates-pages-exhibit').html());
        self.element.html(template);
        self.exhibit=$('#exhibit');

        self.EvAg.trigger("filter/change",self.filter);

        self.exhibit.isotope({
            itemSelector: '.item_wrap,.no_filter',
            masonry:{
                columnWidth: 5
            },
            getSortData: {
                date: function ($elem) {
                    return $elem.attr("date");
                }
            },
            filter: self.filter
        });

        if(data.partner){
            self.addTitle(data.partner.name, data.partner.logo);
        } else if (self.id == "friends") {
            self.addTitle(data.echoedUserName);
        } else {
            self.addTitle(self.contentTitle);
        }

        if(data.echoes){
            if(data.echoes.length > 0){
                self.addProducts(data);
                self.productCount += data.echoes.length;
                self.EvAg.bind('infiniteScroll', self.next);
            }
            else{
                self.nextInt = null;
                var noEchoDiv = $('<div></div>').addClass("no-echoes").html("There are currently no Echoes.");
                noEchoDiv.appendTo(exhibit);
                self.EvAg.trigger("infiniteScroll/unlock");
            }
        }
        if(data.product){
            var productModel = new Echoed.Models.Product(data.product);
            self.addProduct(productModel, self.filter);
        }
        if(data.questions){
            $.each(data.questions, function(index, question){
                self.addQuestion(question);
            });
        }
        if(data.facebookPosts){
            $.each(data.facebookPosts, function(index, facebookPost){
                self.addFacebookPost(facebookPost)
            });
        }
        if(data.echoedUsers){
            $.each(data.echoedUsers, function(index,echoedUser){
            });
        }
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
        self.exhibit.isotope({filter: '.no_filter,#exhibit .item_wrap' + selector});
    },
    next: function(){
        var self = this;
        if(self.nextInt != null){
            self.EvAg.trigger('infiniteScroll/lock');
            $.ajax({
                url: self.jsonUrl + "?page=" + self.nextInt,
                xhrFields: {
                    withCredentials: true
                },
                dataType: 'json',
                success: function(data){
                    if(data.echoes.length > 0){
                        self.addProducts(data);
                        self.nextInt++;

                    }
                    else{
                        self.nextInt = null;
                    }
                }
            });
        }
    },
    addTitle: function(title, image){
        var self = this;
        var titleDiv = $('<div></div>').addClass('item_wrap').addClass('no_filter').attr("id","title");
        //if(image) titleDiv.append($('<img />').attr("src", image).attr("height", "40px"));
        titleDiv.append(title);

        self.exhibit.isotope('insert', titleDiv);
    },
    complete: function(){
        var self = this;
        //self.EvAg.bind('infiniteScroll', self.next);
    },
    addProfile: function(){
        var self = this;
        var profileDiv = $('<div></div>').addClass('profile').addClass('item_wrap');
        var profileComponent = new Echoed.Views.Components.Profile({ el: profileDiv, EvAg: self.EvAg });
        self.exhibit.isotope('insert', profileDiv);
    },
    addFacebookPost: function(facebookPost){
        var self = this;
        var fbDiv = $('<div></div>').addClass("item_wrap").addClass('fbp');
        var fbComp = new Echoed.Views.Components.FacebookPost({ el: fbDiv, facebookPost: facebookPost, EvAg: self.EvAg});
        self.exhibit.isotope('insert',fbDiv);
    },
    addQuestion: function(question){
        var self = this;
        var questionDiv = $('<div></div>').addClass("item_wrap").addClass('fbp');
        var questionComp = new Echoed.Views.Components.Question({ el: questionDiv, question: question, EvAg: self.EvAg});
        self.exhibit.isotope('insert', questionDiv);
    },
    addProducts: function(data){
        var self = this;
        var productsFragment = $('<div></div>');
        var lastDiv;
        $.each(data.echoes, function(index, product){

            var productDiv = $('<div></div>');
            var productModel = new Echoed.Models.Product(product);
            var productComponent = new Echoed.Views.Components.Product({el:productDiv, model:productModel, EvAg: self.EvAg });
            self.EvAg.trigger('Filter/add',product.echoCategory,product.echoCategory);
            productsFragment.append(productDiv);
        });
        self.exhibit.isotope('insert',productsFragment.children(), function(){
            self.EvAg.trigger('infiniteScroll/unlock');
        });
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

Echoed.Views.Components.Partner = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.partner = options.partner;
    },
    render: function(){
        var self = this;
        this.element.empty();
        var template = _.template($('#templates-components-ptnr').html());
        this.element.html(template);
    }
});

Echoed.Views.Components.Actions = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.element.show();
        this.render();
    },
    events: {
        'click .action-button': 'click'
    },
    render: function(){
        var self = this;
        this.element.empty();
        this.element.append($("<div act='story' class='action-button'>Tell a Story</div>"));
    },
    click: function(e){
        var self = this;
        var target = $(e.target);
        var action = target.attr("act");
        self.EvAg.trigger("field/show", action);
    }
});

Echoed.Views.Components.Question = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.question = options.question;
        this.render();
    },
    render: function(){
        var self = this;
        self.element.empty();
        var template = _.template($('#templates-components-fbp').html());
        self.element.html(template);
        self.element.attr("date", self.question.createdOn);
        self.element.find(".message").html(self.question.message);
    }
});

Echoed.Views.Components.FacebookPost = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.facebookPost = options.facebookPost;
        this.render();
    },
    render: function(){
        var self = this;
        this.element.empty();
        var template = _.template($('#templates-components-fbp').html());
        this.element.html(template);
        this.element.attr("date", self.facebookPost.createdOn);
        this.element.find(".message").html(self.facebookPost.message);
    }
});

Echoed.Views.Components.Profile = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.render();
    },
    render: function(){
        var self = this;
        this.element.empty();
        var template = _.template($('#templates-components-profile').html());
        this.element.html(template).addClass('no_filter');
        $.ajax({
            url: Echoed.urls.api + "/user/me",
            xhrFields: {
                withCredentials: true
            },
            dataType: 'json',
            success: function(data){
                var un = self.element.find('.profile_username').html(data.echoedUser.name);
                var rew = self.element.find('.profile_reward').html("$" + data.totalCredit.toFixed(2));
                var tc = self.element.find('.profile_referrers').html(data.totalVisits);
            }
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
        $('#content-title').html("Friends");
        jsonUrl = Echoed.urls.api + "/api/me/friends";
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
    },
    events:{
        "click": "triggerClick"
    },
    triggerClick: function(){
    }
});

Echoed.Views.Components.Dropdown = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'addSelector','triggerClick','updateLabel','init','hide');

        this.name = options.Name;
        this.state = 0;
        this.EvAg = options.EvAg;
        this.categories = new Array();
        this.EvAg.bind(this.name + '/add', this.addSelector);
        this.EvAg.bind('filter/change',this.triggerClick);
        this.EvAg.bind('filter/init', this.init);
        this.EvAg.bind('filter/hide', this.hide);
        this.el = options.el;
        this.element = $(this.el);
        this.body = this.element.find('.dd-body');
        this.header = this.element.find('.dd-header');
        this.list = {};
    },
    init: function(baseUrl){
        var self = this;
        self.baseUrl = baseUrl;
        self.selected = "All";
        self.body.empty();
        self.list = {};
        this.render();
        self.element.fadeIn();
    },
    hide: function(){
        var self = this;
        self.element.fadeOut();
    },
    events: {
        "click .dd-header": "click",
        "click li": "highlight"
    },
    render: function(){
        var self = this;
        self.header.html('<strong>' + this.name + ': </strong>' + this.selected);
    },
    updateList: function(){
        var self = this;
        self.body.empty();
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
        ul.appendTo(self.body);
        this.dropDown = ul;
        var li = $('<li></li>').appendTo(ul);
        var anc =$('<a></a>').attr("id","All").attr("href", this.baseUrl).html("All").appendTo(li);
        for(var id in this.list){
            anc = $('<a></a>').attr("id",id).attr("href", this.baseUrl + encodeURIComponent(this.name + "-" + id)).html(id).appendTo(li);
        }
    },
    highlight: function(e){
    },
    addSelector: function(selector,text){
        if(!this.list[text]){
            this.list[text] =  selector;
            this.updateList();
        }
    },
    showList: function(){
        this.body.slideDown();
    },
    hideList: function(){
        this.body.slideUp();
    },
    click: function(e){
        var self = this;
        if(self.state === 0){
            self.showList();
            self.state = 1
        } else {
            self.hideList();
            self.state = 0;
        }
    },
    updateLabel: function(){
        this.element.find('.dd-header').html('<strong>' + this.name + ': </strong>' + this.selected);
    },
    triggerClick: function(e){
        if(e === "*" || e === "undefined" || typeof(e) === "undefined")
            this.selected = "All";
        else
            this.selected = e.split("-")[1];
        this.updateLabel();
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

Echoed.Views.Components.Product = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'showOverlay','hideOverlay','enlarge','shrink','click','clickPartner');
        this.el = options.el;
        this.EvAg = options.EvAg;
        this.state =0;
        this.render();
    },
    events:{
        "mouseenter": "showOverlay",
        "mouseleave": "hideOverlay",
        "click .icl_pn" : "clickPartner",
        "click .exhibit_image": "click"
    },
    render: function(){
        var template = _.template($('#templates-components-product').html());
        var self = this;
        var landingUrl = Echoed.urls.api + "/echo/" + this.model.get("echoId");
        var imageUrl =   this.model.get("image").preferredUrl;
        var imageWidth = this.model.get("image").preferredWidth;
        var imageHeight = this.model.get("image").preferredHeight;
        this.el.attr("date", this.model.get("echoBoughtOn"));
        this.el.attr("productId", this.model.get("echoProductId"));
        this.el.attr("partnerId", this.model.get("partnerId"));
        this.el.attr("id", this.model.get("echoId"));
        this.el.addClass("item_wrap").addClass('Brand-' + encodeURIComponent(this.model.get("echoBrand"))).addClass('Filter-' + encodeURIComponent(this.model.get("echoCategory"))).html(template).attr("href", landingUrl);
        var hover = this.el.find(".item_hover_wrap");
        var img = this.el.find("img");
        var text = this.el.find(".item_text");
        if(this.model.get("echoProductName")){
            hover.append(this.model.get("echoProductName") + '<br/>');
            text.prepend(this.model.get("echoProductName")+'<br/>');
        }
        if(this.model.get("partnerName")){
            text.prepend('<span class="icl_pn"><strong>'+this.model.get("partnerName") + '</strong></span><br/>');
            hover.append(this.model.get("partnerName") + '<br/>');
        }
        if(this.model.get("echoedUserName"))
            hover.append('<span class="highlight"><strong>' + this.model.get("echoedUserName") + '</strong></span><br/>');
        if(typeof(this.model.get("echoCredit")) == 'number'){
            hover.append("<span class='highlight'><strong>Reward: $" + this.model.get("echoCredit").toFixed(2) +'</strong></span><br/>');
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
                hover.append("<span class='highlight'><strong>Days Left: "+ (daysleft + 1) + "</strong></span><br/>");
                self.showOverlay();
                var t = setTimeout(self.hideOverlay, 3000);
                img.addClass("open-echo");
                var visits = this.model.get("echoTotalClicks");
            }
        }
        return this;
    },
    showOverlay: function(){
        //this.el.find('.item_hover').slideDown('fast');
    },
    hideOverlay: function(){
        //this.el.find('.item_hover').slideUp('fast');
    },
    enlarge: function(){
        var self = this;
        self.el.css({
            "z-index" : "99999"
        });
        self.EvAg.trigger("item/enlarge");
        self.el.find('.item_content').fadeOut(function(){
            self.el.find('.item_content_large').fadeIn(function(){
                self.EvAg.bind("item/enlarge", self.shrink);
                self.EvAg.trigger("exhibit/relayout",self.el);
            });
        });
        this.state = 1;
    },
    shrink: function(){
        var self= this;
        self.el.css({
            height : "",
            width : "",
            "z-index" : ""
        });
        self.state = 0;
        self.el.find('.item_content').fadeIn();
        self.el.find('.item_content_large').hide();
    },
    clickPartner: function(e){
        var self = this;
        window.location.hash = "#partners/" + this.el.attr('partnerId');
    },
    click: function(e){
        var self = this;
        var href = this.el.attr("href");
        window.open(href);
    }
});
