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
        var exhibit = new Echoed.Views.Pages.Exhibit({ el: '#content', EvAg: EventAggregator });
        var friends = new Echoed.Views.Pages.Friends({ el: '#content', EvAg: EventAggregator });
        var actions = new Echoed.Views.Components.Actions({ el: '#actions', EvAg: EventAggregator });
        var field = new Echoed.Views.Components.Field({ el: '#field', EvAg: EventAggregator });
        var story = new Echoed.Views.Components.Story({ el: '#story', EvAg: EventAggregator});
        var fade = new Echoed.Views.Components.Fade({ el: '#fade', EvAg: EventAggregator });
        Backbone.history.start();
    }
};

Echoed.Views.Components.Fade = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'show','hide');
        this.EvAg = options.EvAg;
        this.el = options.el;
        this.element = $(this.el);
        this.EvAg.bind("fade/show", this.show);
        this.EvAg.bind("fade/hide", this.hide);
    },
    show: function(){
        var self = this;
        var arrPageSizes = self.___getPageSize();
        $("html,body").addClass("noScroll");
        self.element.fadeIn();
    },
    hide: function(){
        var self = this;
        self.element.fadeOut();
        $("html,body").removeClass("noScroll");
    },
    ___getPageSize: function() {
        var xScroll, yScroll;
        if (window.innerHeight && window.scrollMaxY) {
            xScroll = window.innerWidth + window.scrollMaxX;
            yScroll = window.innerHeight + window.scrollMaxY;
        } else if (document.body.scrollHeight > document.body.offsetHeight) { // all but Explorer Mac
            xScroll = document.body.scrollWidth;
            yScroll = document.body.scrollHeight;
        } else { // Explorer Mac...would also work in Explorer 6 Strict, Mozilla and Safari
            xScroll = document.body.offsetWidth;
            yScroll = document.body.offsetHeight;
        }
        var windowWidth, windowHeight;
        if (self.innerHeight) {    // all except Explorer
            if (document.documentElement.clientWidth) {
                windowWidth = document.documentElement.clientWidth;
            } else {
                windowWidth = self.innerWidth;
            }
            windowHeight = self.innerHeight;
        } else if (document.documentElement && document.documentElement.clientHeight) { // Explorer 6 Strict Mode
            windowWidth = document.documentElement.clientWidth;
            windowHeight = document.documentElement.clientHeight;
        } else if (document.body) { // other Explorers
            windowWidth = document.body.clientWidth;
            windowHeight = document.body.clientHeight;
        }
        // for small pages with total height less then height of the viewport
        if (yScroll < windowHeight) {
            pageHeight = windowHeight;
        } else {
            pageHeight = yScroll;
        }
        // for small pages with total width less then width of the viewport
        if (xScroll < windowWidth) {
            pageWidth = xScroll;
        } else {
            pageWidth = windowWidth;
        }
        pageHeight= windowHeight;
        pageWidth = windowWidth;
        arrayPageSize = new Array(pageWidth, pageHeight, windowWidth, windowHeight);
        return arrayPageSize;
    }
});

Echoed.Models.Product = Backbone.Model.extend({
    initialize: function(){
    }
});

Echoed.Router = Backbone.Router.extend({
    initialize: function(options) {
        _.bindAll(this,'me','friends','explore', 'story','resetHash');
        this.EvAg = options.EvAg;
        this.EvAg.bind("hash/reset", this.resetHash);
        this.page = null;
    },
    routes:{
        "_=_" : "fix",
        "": "explore",
        "explore": "explore",
        "explore/": "explore",
        "me/friends": "friends",
        "me/": "me",
        "me": "me",
        "user/:id": "user",
        "partner/:name/": "partnerFeed",
        "partner/:name": "partnerFeed",
        "story/:id/edit": "editStory",
        "story/:id": "story",
        "write/:type/:id" : "writeStory",
        "write/" : "writeStory",
        "write": "writeStory"
    },
    editStory: function(){
        if(this.page != window.location.hash) {
            this.page = window.location.hash;
            this.EvAg.trigger("exhibit/init", { Type: "editStory"});
        }
    },
    fix: function(){
        window.location.href = "#";
    },
    explore: function(){
        if(this.page != window.location.hash){
            this.page = "";
            _gaq.push(['_trackPageview', this.page]);
            this.EvAg.trigger('exhibit/init', { Type: "explore"});
            this.EvAg.trigger("page/change","explore");
        }
    },
    partnerFeed: function(partnerId) {
        if(this.page != window.location.hash){
            this.page = window.location.hash;
            this.EvAg.trigger('exhibit/init', { Type: "partner", partnerId: partnerId });
            _gaq.push(['_trackPageview', this.page]);
            this.EvAg.trigger("page/change","partner");
        }
    },
    me: function() {
        if(this.page != window.location.hash){

            //this.page= window.location.hash;
            this.page = "#me";
            this.EvAg.trigger('exhibit/init', { Type: "exhibit"});
            _gaq.push(['_trackPageview', this.page]);
            this.EvAg.trigger("page/change","exhibit");
        }
    },
    writeStory: function(type, id){
        if(this.page === null){
            switch(type){
                case "partner":
                    this.partnerFeed(id);
                    this.page = "#partner/" + id;
                    break;
                default:
                    this.explore();
                    this.page = "";
                    break;
            }
        }
        this.oldPage = this.page;
        this.EvAg.trigger("field/show",id , type);
    },
    story: function(id){
        if(this.page === null) {
            this.explore();
            this.page = "";
        }
        this.oldPage = this.page;

        this.EvAg.trigger("story/show", id);
        this.EvAg.trigger("page/change", "story");
    },
    resetHash: function(){
        if(this.oldPage){
            window.location.hash = this.oldPage;
        } else {
            window.location.hash = "#";
        }

    },
    friends: function() {
        if(this.page != window.location.hash){
            this.page = window.location.hash;
            this.EvAg.trigger('friends/init');
            this.EvAg.trigger("page/change","friends");
            _gaq.push(['_trackPageview', this.page]);
        }
    },
    user: function(id){
        if(this.page != window.location.hash){
            this.page = window.location.hash;
            this.EvAg.trigger('exhibit/init', { Type: 'friend', Id: id});
            _gaq.push(['_trackPageview', this.page]);
        }
    }
});

Echoed.Views.Components.Field = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this, 'render','unload','load','loadStoryTemplate','submitInitStory','loadChapterTemplate','loadChapterHelper','submitChapter');
        this.element = $(options.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind("field/show", this.load);
        this.prompts = [];
    },
    events: {
        "click .field-close" : "close",
        "click .field-submit" : "submitInitStory",
        "click input[type=text]" : "loadChapterHelper",
        "click .chapter-submit": "submitChapter"
    },
    load: function(id, type){
        var self = this;
        var jsonUrl =  Echoed.urls.api + "/story";
        var loadData = {};
        self.data = {};

        switch(type){
            case "story":
                loadData.storyId = id;
                break;
            case "echo":
                loadData.echoId = id;
                break;
            case "partner":
                loadData.partnerId = id;
                break;
        }
        if(Echoed.echoedUser){
            $.ajax({
                url: jsonUrl,
                type: "GET",
                xhrFields: {
                    withCredentials: true
                },
                data: loadData,
                dataType: 'json',
                success: function(initStoryData){
                    self.data = initStoryData;
                    self.render();
                }
            });
        } else {
            self.data = {};
            if(type == "partner"){
                $.ajax({
                    url: Echoed.urls.api + "/api/partner/" + id,
                    type: "GET",
                    xhrFields: {
                        withCredentials: true
                    },
                    dataType: 'json',
                    success: function(data){
                    self.renderLogin(data);
                    }
                });
            } else {
                self.renderLogin();
            }
        }
    },
    submitInitStory: function(){
        var self = this;
        var title = $.trim(self.element.find("#story-name").val());
        var productFrom = $.trim($('#story-from').val());
        var echoId = null;
        var storyData = {};
        if(self.data.echo)
            storyData = {
                title: title,
                echoId : self.data.echo.id,
                imageId : self.data.imageId,
                productInfo: productFrom
            };
        else
            storyData = {
                title: title,
                imageId : self.data.imageId,
                productInfo: productFrom
            };

        if(!self.data.imageId){
            alert("Please select a photo for the product");
        } else if(self.data.title == ""){
            alert("Please title your product story");
        } else if(self.data.productFrom == "") {
            alert("Please enter where the product is from");
        } else {
            $.ajax({
                url: Echoed.urls.api + "/story",
                type: "POST",
                xhrFields: {
                    withCredentials: true
                },
                dataType: 'json',
                data: storyData,
                success: function(createStoryResponse){
                    self.load(createStoryResponse.id, "story");
                }
            });
        }
    },
    loadChapterHelper: function(){
        var self = this;
        self.chapterHelper.fadeIn();
    },
    loadChapterTemplate: function(){
        var self = this;
        self.template = _.template($('#templates-components-story-edit').html());
        self.element.html(self.template);
        self.element.chapterList = $('#chapter-list');
        self.element.chapterHelper = self.element.find(".chapter-title-helper");
        self.element.chapterHelper.append($("<div class='s-e-b-r-h'>Chapter Ideas</div>"));
        $.each(self.data.storyPrompts.prompts, function(index, prompt){
            self.element.chapterHelper.append($("<div class='s-e-b-r-i'></div>").append(prompt))
        });
        $("#story-title").html(self.data.storyFull.story.title);
        $("#story-from").html(self.data.storyFull.story.productInfo);
        $("#story-title-photo").attr("src", self.data.storyFull.story.image.preferredUrl);
        var count = 0;
        $.each(self.data.storyFull.chapters, function(index, chapter){
            count = count + 1;
            var chapterDiv = $('<div class="chapter"></div>').addClass("clearfix");
            chapterDiv.append($('<div class="chapter-number"></div>').append(count));
            var chapterTextContainer = $('<div class="chapter-text-container"></div>');
            chapterTextContainer.append($('<div class="chapter-title"></div>').append(chapter.title));
            chapterTextContainer.append($('<div class="chapter-text"></div>').append(chapter.text));
            chapterDiv.append(chapterTextContainer);

            var chapterPhotos = $('<div class="chapter-photos"></div>');
            $.each(self.data.storyFull.chapterImages, function(index, chapterImage){
                if(chapterImage.chapterId == chapter.id){
                    var chapterImg = $('<div class="chapter-photo"></div>')
                    chapterImg.append($('<img />').attr("height", 50).attr("src",chapterImage.image.preferredUrl));
                    chapterPhotos.append(chapterImg);
                }
            });
            chapterDiv.append(chapterPhotos);
            self.element.chapterList.append(chapterDiv);
        });

        var chapterPhotos = self.element.find(".thumbnails");
        self.currentChapter = {};
        self.currentChapter.images = [];
        self.currentChapter.title = "";
        self.currentChapter.text = "";
        var uploader = new qq.FileUploader({
            element: document.getElementsByClassName('photo-upload')[0],
            action: '/image',
            debug: true,
            allowedExtensions: ['jpg', 'jpeg', 'png', 'gif'],
            onSubmit: function(id, fileName) {

            },
            onComplete: function(id, fileName, response) {
                var thumbDiv = $('<div></div>').addClass("thumb");
                var photo = $('<img />').attr('src', response.url);
                thumbDiv.append(photo).hide().appendTo(chapterPhotos).fadeIn();
                self.currentChapter.images.push(response.id);
            }
        });
        self.show();
    },
    submitChapter: function(e){
        var self = this;
        self.currentChapter.title = $.trim($('#chapter-title').val());
        self.currentChapter.text = $.trim($('#chapter-text').val());

        if(self.currentChapter.title == ""){
            alert("You must have a title for your chapter");
        } else if(self.currentChapter.text == ""){
            alert("You must have some text for your chapter. Even a single sentence is enough!");
        } else {
            var nextAction = $(e.target).attr('act');
            $.ajax({
                url: Echoed.urls.api + "/story/" + self.data.storyFull.story.id + "/chapter",
                type: "POST",
                xhrFields: {
                    withCredentials: true
                },
                dataType: 'json',
                processData: false,
                contentType: "application/json",
                data: JSON.stringify({
                    title: self.currentChapter.title,
                    text: self.currentChapter.text,
                    imageIds: self.currentChapter.images
                }),
                success: function(chapterSubmitResponse) {
                    switch(nextAction){
                        case 'continue':
                            self.load(self.data.storyFull.story.id, "story");
                            break;
                        case 'finish':
                            window.location.hash = "#story/" + self.data.storyFull.story.id;
                            self.unload();
                            break;
                    }
                }
            });
        }
    },
    unload: function(){
        var self = this;
        self.EvAg.trigger('fade/hide');
        self.EvAg.trigger('hash/reset');
        self.element.fadeOut();
        self.element.empty();
        self.data = {};
    },
    renderLogin: function(data){
        var self = this;
        self.template = _.template($('#templates-components-story-login').html());
        self.element.html(self.template);
        var facebookLoginUrl = Echoed.facebookLogin.head +
            encodeURIComponent(Echoed.facebookLogin.redirect + encodeURIComponent(window.location.hash)) +
            Echoed.facebookLogin.tail;
        var twitterLoginUrl = Echoed.twitterUrl + encodeURIComponent(window.location.hash);
        self.element.find("#field-fb-login").attr("href", facebookLoginUrl);
        self.element.find("#field-tw-login").attr("href", twitterLoginUrl);
        var body = self.element.find(".field-login-body");
        if(data.partner){
            var bodyText = data.partner.name + " wants to hear your story. Share your story and have it featured on the " + data.partner.name + " page. (click here to see other stories from " + data.partner.name + ")"  ;
            var bodyTextNode = $('<div class="field-login-body-text"></div>').append(bodyText);
            body.append(bodyTextNode);
        }

        self.show();
    },
    render: function(){
        var self = this;
        self.element.empty();
        if(self.data.storyFull)
            self.loadChapterTemplate();
        else
            self.loadStoryTemplate();
    },
    loadStoryTemplate: function(){
        var self = this;
        self.template = _.template($('#templates-components-story-input').html());
        self.element.html(self.template);
        self.data.imageId = null;
        if (self.data.partner){
            $("#story-from").val(self.data.partner.name).attr("readonly",true);
            self.element.find('.field-title').html("Share Your " + self.data.partner.name + " Story");
        }
        if(self.data.echo){
            $("#field-photo").attr("src", self.data.echo.image.sizedUrl);
            self.data.imageId = self.data.echo.image.id;
            $("#story-from").val(self.data.partner.name).attr("readonly",true);
            $("#story-name").val(self.data.echo.productName);
        } else {
            $("#field-photo-upload").show();
            var uploader = new qq.FileUploader({
                element: document.getElementById('field-photo-upload'),
                action: '/image',
                debug: true,
                allowedExtensions: ['jpg', 'jpeg', 'png', 'gif'],
                onSubmit: function(id, fileName) {

                },
                onComplete: function(id, fileName, response) {
                    $("#field-photo").attr("src", response.url);
                    self.data.imageId = response.id;

                }
            });
        }
        self.show();
    },
    show: function(){
        var self = this;
        self.EvAg.trigger('fade/show');
        self.element.css({
            "margin-left" : -(self.element.width()/2)
        });
        self.element.fadeIn();
        $("#story-name").focus();
    },
    close: function(){
        var self = this;
        self.element.fadeOut();
        self.element.empty();
        self.EvAg.trigger('fade/hide');
        self.EvAg.trigger('hash/reset');
    }
});

Echoed.Views.Components.InfiniteScroll = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'triggerScroll', 'lock', 'unlock', 'on', 'off','scrollUp');
        this.EvAg = options.EvAg;
        this.EvAg.bind("triggerInfiniteScroll", this.triggerScroll);
        this.EvAg.bind("infiniteScroll/lock", this.lock);
        this.EvAg.bind("infiniteScroll/unlock", this.unlock);
        this.EvAg.bind("fade/show", this.off);
        this.EvAg.bind("fade/hide", this.on);
        this.EvAg.bind("infiniteScroll/on", this.on);
        this.EvAg.bind("infiniteScroll/off", this.off);
        this.EvAg.bind('page/change', this.scrollUp);
        this.element = $(options.el);
        this.locked = false;
        var self = this;
        $(window).scroll(function(){
            if($(window).scrollTop() + 600 >= $(document).height() - $(window).height() && self.locked == false && self.status == true){
                self.EvAg.trigger("infiniteScroll");
            }
        });
    },
    scrollUp: function(){
        $("html, body").animate({scrollTop: 0 }, 300);
    },
    on: function(){
        var self = this;
        self.status = true;
    },
    off: function(){
        var self = this;
        self.status = false;
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
        if($(window).scrollTop() + 600 >= $(document).height() - $(window).height() && self.locked == false && self.status == true){
            self.EvAg.trigger("infiniteScroll");
        }
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
        _.bindAll(this,'render','next','init','relayout', 'addProducts','addTitle');
        this.EvAg = options.EvAg;
        this.EvAg.bind('exhibit/relayout', this.relayout);
        this.EvAg.bind('exhibit/init', this.init);
        this.EvAg.bind('infiniteScroll', this.next);
        this.element = $(this.el);
        this.template = _.template($('#templates-pages-exhibit').html());
        this.element.html(this.template);
        this.exhibit = $('#exhibit');
    },
    init: function(options){
        var self = this;
        self.EvAg.trigger('infiniteScroll/on');
        if (self.exhibit.isotope){
            self.exhibit.isotope("destroy");
        }
        self.exhibit.empty();
        self.exhibit.isotope({
            itemSelector: '.item_wrap,.no_filter',
            masonry:{
                columnWidth: 5
            }
        });

        self.contentDescription = "";
        self.showDate = false;
        switch(options.Type){
            case "friend":
                self.jsonUrl = Echoed.urls.api + "/api/user/" + options.Id;
                self.contentTitle = "Your Friends";
                self.id = "friends";
                self.nextInt = 1;
                break;
            case "partner":
                self.jsonUrl = Echoed.urls.api + "/api/partner/" + options.partnerId;
                self.contentTitle = options.Name;
                self.id = "partner";
                self.showDate = 1;
                self.nextInt = 1;
                break;
            case "explore":
                self.jsonUrl = Echoed.urls.api + "/api/me/feed";
                self.contentTitle = "Just Purchased";
                self.contentDescription = "See all the purchased products at our partners as they're bought ";
                self.id= "explore";
                self.nextInt = 1;
                break;
            case "explore/friends":
                self.jsonUrl = Echoed.urls.api + "/api/me/feed/friends";
                self.id = "explore/friends";
                self.nextInt = 1;
                break;
            case "exhibit":
                self.jsonUrl = Echoed.urls.api + "/api/me/exhibit";
                self.contentTitle = "Me";
                self.contentDescription = "All the products you've shared and the rewards you've earned";
                self.id = null;
                self.nextInt = 1;
                break;
        }
        self.EvAg.trigger('infiniteScroll/lock');
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
        if(data.partner){
            self.contentDescription = "The recent products purchased at " + data.partner.name;
            self.addTitle(data.partner.name, data.partner.logo);
        } else if (self.id == "friends") {
            self.contentDescription = "Products purchased and shared by " + data.echoedUser.name;
            self.addTitle(data.echoedUser.name);
        } else if (self.id != "story"){
            self.addTitle(self.contentTitle);
        }
        if(!Echoed.echoedUser) self.addLogin();
        if(data.stories){
            self.addStories(data);
        }
        if(data.echoes){
            self.addProducts(data);
        } else {
            self.nextInt = null;
            self.EvAg.trigger("infiniteScroll/unlock");
        }

    },
    relayout: function(e){
        var self = this;
        self.exhibit.isotope('reLayout', function(){
            $("html, body").animate({scrollTop: e.offset().top - 90 }, 300);
        });
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
                        self.EvAg.trigger("infiniteScroll/unlock");
                    }
                }
            });
        }
    },
    addTitle: function(title, image){
        var self = this;
        var titleDiv = $('<div></div>').addClass('item_wrap').addClass('no_filter').attr("id","title");
        var titleContainer = $('<div></div>').addClass("title-container");
        titleDiv.append(titleContainer);
        titleContainer.append($('<div></div>').addClass('title-text').append(title));
        titleContainer.append($('<div></div>').addClass('title-description').append(self.contentDescription));
        self.exhibit.isotope('insert', titleDiv);
    },
    addLogin: function(){
        var self = this;
        var facebookLoginUrl = Echoed.facebookLogin.head +
                               encodeURIComponent(Echoed.facebookLogin.redirect + encodeURIComponent(window.location.hash)) +
                               Echoed.facebookLogin.tail;
        var twitterLoginUrl = Echoed.twitterUrl + encodeURIComponent(window.location.hash);

        var loginDiv = $('<div></div>').addClass('item_wrap');
        var template = _.template($("#templates-components-login").html());
        loginDiv.html(template);
        loginDiv.find("#facebookLogin").attr("href",facebookLoginUrl);
        loginDiv.find("#twitterLogin").attr("href", twitterLoginUrl);
        self.exhibit.isotope('insert', loginDiv)
    },
    addStories: function(data){
        var self = this;
        var storiesFragment = $('<div></div>');
        $.each(data.stories, function(index, story){
            var storyDiv = $('<div></div>').addClass('item_wrap');
            var storyComponent = new Echoed.Views.Components.StoryBrief({el : storyDiv, data: story, EvAg: self.EvAg});
            storiesFragment.append(storyDiv);

        });
        self.exhibit.isotope('insert', storiesFragment.children());
    },
    addProducts: function(data){
        var self = this;
        var productsFragment = $('<div></div>');
        $.each(data.echoes, function(index, product){
            var productDiv = $('<div></div>');
            var productModel = new Echoed.Models.Product(product);
            var productComponent = new Echoed.Views.Components.Product({el:productDiv, model:productModel, EvAg: self.EvAg });
            self.EvAg.trigger('Browse/add',product.echoCategory,product.echoCategory);
            productsFragment.append(productDiv);
        });
        self.exhibit.isotope('insert',productsFragment.children(), function(){
            self.EvAg.trigger('infiniteScroll/unlock');
        });
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
        this.element.append($("<div class='action-button'>Share a Story</div>"));
    },
    click: function(e){
        var self = this;
        window.location.hash = "#write/";
    }
});

Echoed.Views.Pages.Friends = Backbone.View.extend({
    el: '#content',
    initialize: function(options){
        _.bindAll(this, 'init');
        this.EvAg = options.EvAg;
        this.EvAg.bind('friends/init', this.init);
        this.element = $(this.el);
        this.exhibit = $('#exhibit');
    },
    init: function(){
        var self = this;
        this.EvAg.trigger('infiniteScroll/off');
        self.exhibit.empty();
        self.exhibit.isotope("destroy");
        var jsonUrl = Echoed.urls.api + "/api/me/friends";
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
                        var  a = $('<a></a>').attr("href","#user/" + friend.toEchoedUserId);
                        if(friend.facebookId != null)
                            img = $('<img />').attr("height","50px").attr("src","http://graph.facebook.com/" + friend.facebookId + "/picture");
                        else
                            img = $('<img />').attr("src", "http://api.twitter.com/1/users/profile_image/" + friend.twitterId);
                        friendImage.append(img);
                        var friendDiv = $('<div></div>').addClass("friend").append(friendImage).append(friendText).appendTo(a);
                        a.appendTo(self.exhibit);
                    });
                }
                else{
                }
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

Echoed.Views.Components.Select = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'render','click','open','selectOption','close');
        this.el = options.el;
        this.element = $(options.el);
        this.optionsList = options.optionsList;
        this.openState = false;
        this.default = "Select a Prompt Or Write Your Own";
        this.render();
    },
    events: {
        "click .field-question-label" : "click",
        "click .field-question-option" : "selectOption",
        "keyup :input": "keyPress"
    },
    render: function(){
        var self = this;
        self.input = $("<input type='text'>");
        self.input.val(self.default);
        self.label = $("<div class='field-question-label'></div>");
        self.label.append(self.input);
        self.element.append(self.label);
        self.options[0] = $("<div class='field-question-option'>Why I Bought This</div>").css({"display": "none"});
        self.element.append(self.options[0]);
        self.options[1] = $("<div class='field-question-option'>This Is For Alex</div>").css({"display": "none"});
        self.element.append(self.options[1]);
    },
    keyPress: function(e){
        switch(e.keyCode){
            case 13:
                this.close();
        }
    },
    click: function(){
        var self = this;
        if(self.openState == false )
            self.open();
        else
            self.openState = false
    },
    selectOption: function(e){
        var self = this;
        var target = $(e.target);
        self.input.val(target.html());
        self.close();

    },
    close: function(){
        var self = this;
        self.openState = false;
        self.element.find(".field-question-option").hide();
        if(self.input.val() == "")
            self.input.val(self.default);
    },
    open: function(){
        var self = this;
        self.openState = true;
        self.input.focus();
        self.input.select();
        if(self.input.val() == self.default)
            self.input.val("");
        self.element.find(".field-question-option").show();
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

Echoed.Views.Components.Story = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'render', 'load','createComment', 'renderCover', 'renderImage');
        this.el = options.el;
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind('story/show', this.load);
    },
    events: {
        "click .echo-s-h-close" : "close",
        "click .comment-submit": "createComment",
        "click .echo-chapters" : "tabClick",
        "click .echo-s-b-thumbnail": "renderImage",
        "click a": "close"
    },
    load: function(id){
        var self = this;
        $.ajax({
            url: Echoed.urls.api + "/api/story/" + id,
            type: "GET",
            xhrFields: {
                withCredentials: true
            },
            success: function(data){
                self.data = data;
                self.render();
            }
        });
    },
    render: function(){
        var template = _.template($('#templates-components-story').html());
        var self = this;
        self.element.html(template);
        self.images = {};
        self.images.cover = [self.data.story.image];
        $.each(self.data.chapterImages, function(index, chapterImage){
            if(!self.images[chapterImage.chapterId])
                self.images[chapterImage.chapterId] = [];
            self.images[chapterImage.chapterId].push(chapterImage.image);
        });

        self.gallery = self.element.find('.echo-s-b-gallery');
        self.element.find('.echo-s-h-t-t').html(self.data.story.title);
        self.element.find('.echo-s-h-i-i').attr("src",self.data.story.image.originalUrl);

        var userLink = '<a href="#user/' + self.data.echoedUser.id + '">' + self.data.echoedUser.name + '</a>';

        self.itemNode = $("<div class='echo-s-b-item'></div>");
        self.itemImageContainer = $("<div class='echo-s-b-i-c'></div>");
        self.element.find('.echo-s-h-t-n').html("by " + userLink);
        self.img = $("<img />");
        self.itemNode.append(self.itemImageContainer.append(self.img)).appendTo(self.gallery);
        self.thumbnails = $("<div class='echo-s-b-thumbnails'></div>").appendTo(self.gallery);

        self.renderTabs();
        self.renderComments();
        if(self.data.chapters.length > 0)
            self.renderChapter(0);
        else
            self.renderCover();
        self.EvAg.trigger('fade/show');
        self.element.css({
           "margin-left": -(self.element.width() / 2)
        });
        self.element.fadeIn();
    },
    renderTabs: function(){
        var self = this;
        self.chapterTabs = self.element.find('.echo-chapters');
        self.chapterTabs.append($('<div class="echo-chapter"></div>').append('Cover').addClass("on").attr("tab-id","cover"));
        $.each(self.data.chapters, function(index, chapter){
            self.chapterTabs.append($('<div class="echo-chapter"></div>').append(chapter.title).attr("tab-id",index));
        });
    },
    renderCover: function(){
        var self = this;
        if(self.data.echo){
            self.element.find('.echo-s-b-t-t').html(self.data.echo.productName + "<br/>");
        } else {
            self.element.find('.echo-s-b-t-t').html(self.data.story.productInfo);
            self.element.find('.echo-s-b-t-b').html("");
            self.img.attr('src', self.images['cover'][0].originalUrl);
        }
    },
    tabClick: function(e){
        var self = this;
        var index = $(e.target).attr('tab-id');
        self.chapterTabs.children().removeClass("on");
        $(e.target).addClass("on");
        if(index =="cover"){
            self.thumbnails.empty();
            self.renderCover();
        } else {
            self.thumbnails.empty();
            self.renderChapter(index);
        }
    },
    renderChapter: function(index){
        var self = this;
        console.log(self.data);
        self.currentChapterId = self.data.chapters[index].id;
        self.element.find('.echo-s-b-t-t').html(self.data.chapters[index].title);
        self.element.find('.echo-s-b-t-b').html('"' + self.data.chapters[index].text + '"');
        self.img.attr('src', self.images[self.currentChapterId][0].originalUrl);
        $.each(self.images[self.currentChapterId], function(index, image){
            self.thumbnails.append($("<img />").addClass("echo-s-b-thumbnail").attr("index",index).attr("src", image.originalUrl));
        });
    },
    renderImage: function(e){
        var self = this;
        $('.echo-s-b-thumbnail').removeClass('highlight');
        $(e.target).addClass('highlight');
        self.img.attr('src', self.images[self.currentChapterId][$(e.target).attr("index")].originalUrl);
    },
    renderComments: function(){
        var self = this;
        var commentListNode = self.element.find('.echo-s-c-l');
        var comments = {
            children: []
        };
        $.each(self.data.comments, function(index, comment){
            var parentId = comment.parentCommentId;
            if(parentId == null){
                comments.children.push(comment);
            } else {
                if(comments[parentId] == null){
                    comments[parentId] = {
                        children: []
                    }
                }
                comments[parentId].children.push(comment);
            }
        });
        commentListNode.empty();
        $("#echo-story-comment-ta").val("");
        if(self.data.comments.length > 0) $("#echo-s-c-t-count").html("(" + self.data.comments.length + ")");
        $.each(self.data.comments, function(index,comment){
            var commentUserNode = $('<div class="echo-s-c-l-c-u"></div>').append($("<a></a>").append(comment.echoedUser.name).attr("href","#user/" + comment.echoedUser.id));
            var commentText = $('<div class="echo-s-c-l-c-t"></div>').append(comment.text);
            var commentNode = $('<div class="echo-s-c-l-c"></div>').append(commentUserNode).append(commentText);
            commentListNode.append(commentNode);
        });
    },
    createComment: function(){
        var self = this;
        var storyId = self.data.story.id;
        var chapterId = self.data.chapters[self.currentChapterNum].id;
        var text = $("#echo-story-comment-ta").val();
        $.ajax({
            url: Echoed.urls.api + "/story/" + storyId + "/chapter/" + chapterId + "/comment",
            type: "POST",
            xhrFields: {
                withCredentials: true
            },
            dataType: 'json',
            data: {
                text: text
            },
            success: function(createCommentData) {
                self.data.comments.push(createCommentData);
                self.renderComments();
            }
        });
    },
    close: function(){
        var self = this;
        self.element.fadeOut();
        self.EvAg.trigger("fade/hide");
        self.EvAg.trigger("hash/reset");
    }
});

Echoed.Views.Components.StoryBrief = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'render','click','hideOverlay','showOverlay');
        this.el = options.el;
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.data = options.data;
        this.render();
    },
    events: {
        "click" : "click",
        "mouseenter .story-brief-image-container": "showOverlay",
        "mouseleave .story-brief-image-container": "hideOverlay"
    },
    render: function(){
        var self = this;
        var template = _.template($('#templates-components-story-brief').html());
        self.element.html(template);
        var imageNode = self.element.find(".story-brief-image");
        var textNode = self.element.find(".story-brief-text");
        var overlayNode = self.element.find(".story-brief-overlay-wrap");
        var image = null;
        if(self.data.chapterImages.length > 0)
            image = self.data.chapterImages[0].image
        else
            image = self.data.story.image;

        var hToWidthRatio = image.preferredHeight / image.preferredWidth;
        var width = 260;
        imageNode.attr("src", image.originalUrl).css({
            "height" : width * hToWidthRatio,
            "width" : width
        });
        overlayNode.html(self.data.story.title);
        var photoSrc;
        if(self.data.echoedUser.facebookId)
            photoSrc = "http://graph.facebook.com/" + self.data.echoedUser.facebookId + "/picture";
        textNode.append($("<img height='40px' width='40px' align='absmiddle'/>").attr("src",photoSrc).css({"margin": 5 }));
        textNode.append("Story By " + self.data.echoedUser.name);
        self.element.attr("id", self.data.story.id);
    },
    showOverlay: function(){
        var self = this;
        self.element.find(".story-brief-overlay").fadeIn();
    },
    hideOverlay: function(){
        var self = this;
        self.element.find(".story-brief-overlay").fadeOut();
    },
    click: function(){
        var self = this;
        var id = self.element.attr("id");
        window.location.hash = "#story/" + self.data.story.id;
    }

});

Echoed.Views.Components.Product = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'showOverlay','hideOverlay','enlarge','shrink','click','clickPartner');
        this.el = options.el;
        this.EvAg = options.EvAg;
        this.state = 0;
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
        var imageUrl =   this.model.get("image").originalUrl;
        var imageWidth = this.model.get("image").preferredWidth;
        var imageHeight = this.model.get("image").preferredHeight;
        this.el.attr("date", this.model.get("echoBoughtOn"));
        this.el.attr("productId", this.model.get("echoProductId"));
        this.el.attr("partnerHandle", this.model.get("partnerHandle"));
        this.el.attr("partnerId", this.model.get("partnerId"));
        this.el.attr("id", this.model.get("echoId"));
        this.el.addClass("item_wrap").addClass('Browse-' + encodeURIComponent(this.model.get("echoCategory"))).html(template).attr("href", landingUrl);
        var hover = this.el.find(".item_hover_wrap");
        var img = this.el.find("img");
        var text = this.el.find(".item_text");


        var boughtOnDate = new Date(this.model.get("echoBoughtOn"));
        var todayDate = new Date();
        var dateDiff = todayDate - boughtOnDate;
        var dayDiff = Math.floor((dateDiff)/(1000*60*60*24));
        var hourDiff = Math.floor((dateDiff)/(1000*60*60));
        var minDiff = Math.floor((dateDiff)/(1000*60));
        if(dayDiff >= 1 ){
            text.append(dayDiff + " day(s) ago <br/>");
        } else if (hourDiff >= 1) {
            text.append(hourDiff + " hour(s) ago <br/>");
        } else {
            text.append(minDiff + " minute(s) ago <br/>");
        }

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
            text.append("<span class='highlight'><strong>Reward: $" + this.model.get("echoCredit").toFixed(2) +'</strong></span><br/>');
            this.el.attr("action","story");
        }
        var heightToWidthRatio = imageHeight / imageWidth;
        imageWidth = 260;
        imageHeight = imageWidth * heightToWidthRatio;
        img.attr('src', imageUrl);
        img.attr('width', imageWidth);
        img.attr('height', imageHeight);

        if(this.model.get("echoCreditWindowEndsAt")){
            var then = this.model.get("echoCreditWindowEndsAt");
            var a = new Date();
            var now = a.getTime();
            var diff = then - now;
            var daysleft = parseInt(diff/(24*60*60*1000));
            if(daysleft >= 0){
                text.append("<span class='highlight'><strong>Days Left: "+ (daysleft + 1) + "</strong></span><br/>");
                self.showOverlay();
                var t = setTimeout(self.hideOverlay, 3000);
                img.addClass("open-echo");
                var visits = this.model.get("echoTotalClicks");
            }
        }
        return this;
    },
    showOverlay: function(){
        var img = this.el.find("img")
        img.addClass("highlight");
    },
    hideOverlay: function(){
        this.el.find("img").removeClass("highlight");
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
        if(this.el.attr("partnerHandle"))
            window.location.hash = "#partner/" + this.el.attr('partnerHandle');
        else
            window.location.hash = "#partner/" + this.el.attr('partnerId');
    },
    click: function(e){
        var self = this;
        if(this.el.attr("action") == "story"){
            window.location.hash = "#write/echo/" + this.el.attr("id");
        } else {
            var href = this.el.attr("href");
            window.open(href);
        }
    }
});
