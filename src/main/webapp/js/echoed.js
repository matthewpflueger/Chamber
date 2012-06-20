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
        var filter = new Echoed.Views.Components.Dropdown({ el: '#content-selector', Name: 'Browse', EvAg: EventAggregator});
        var field = new Echoed.Views.Components.Field({ el: '#field', EvAg: EventAggregator });
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
        self.element.css({
            "height": arrPageSizes[1],
            "width": arrPageSizes[2]
        });

        self.element.fadeIn();
    },
    hide: function(){
        var self = this;
        self.element.fadeOut();
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
        _.bindAll(this,'me','friends','explore', 'story');
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
        "friends/exhibit/:id": "friendsExhibit",
        "friends/exhibit/:id/": "friendsExhibit",
        "friends/exhibit/:id/:filter": "friendsExhibit",
        "partners/:name/:filter": "partnerFeed",
        "partners/:name/": "partnerFeed",
        "partners/:name": "partnerFeed",
        "story/:id/edit": "editStory"
        "story/:id": "story"
    },
    editStory: function(){
        this.page = "editStory";
        this.EvAg.trigger("exhibit/init", { Type: "editStory"});
    },
    fix: function(){
        window.location.href = "#";
    },
    explore: function(filter){
        if(this.page != "Explore"){
            this.page = "Explore";
            _gaq.push(['_trackPageview', this.page]);
            this.EvAg.trigger('exhibit/init', { Filter: filter, Type: "explore"});
        }
        else{
            this.EvAg.trigger('filter/change',filter);
        }
        this.EvAg.trigger("page/change","explore");
    },
    exploreFriends: function(filter){
        if(this.page != "Explore/Friends"){
            this.page = "Explore/Friends";
            _gaq.push(['_trackPageview', this.page]);
            this.EvAg.trigger('exhibit/init', { Filter: filter, Type: "explore/friends"});
        }
        else
            this.EvAg.trigger('filter/change',filter);
        this.EvAg.trigger("page/change","explore");
    },
    partnerFeed: function(partnerId,filter) {
        var newPage = "Partner/" + partnerId;
        if(this.page != newPage){
            this.EvAg.trigger('exhibit/init', { Filter: filter, Type: "partners", partnerId: partnerId });
            this.page = newPage;
            _gaq.push(['_trackPageview', this.page]);


        } else{
            this.EvAg.trigger("filter/change", filter);
        }
        this.EvAg.trigger("page/change","partners");
    },
    me: function(filter) {
        if(this.page != "Exhibit"){
            this.EvAg.trigger('exhibit/init', { Filter: filter, Type: "exhibit"});
            this.page = "Exhibit";
            _gaq.push(['_trackPageview', this.page]);

        } else{
            this.EvAg.trigger('filter/change',filter);
        }
        this.EvAg.trigger("page/change","exhibit");
    },
    story: function(id) {
        this.me();
        var storyTest = {
            initStory: function(id) {
                alert("initStory: id = " + id);
                $.ajax({
                    url: Echoed.urls.api + "/story?echoId=" + id,
                    type: "GET",
                    xhrFields: {
                        withCredentials: true
                    },
                    dataType: 'json',
                    success: function(initStoryData){
                        var title = initStoryData.storyPrompts.prompts[0];
                        var imageId = initStoryData.echo.image.id;
                        var echoId = initStoryData.echo.id
                        var storyFull = initStoryData.storyFull;
                        storyTest.initStoryData = initStoryData;
                        if (storyFull) {
                            alert("Story already created, skipping straight to updateStory");
                            storyTest.updateStory(storyFull.id, storyFull.story.image.id);
                        } else {
                            storyTest.createStory(title, imageId, echoId);
                        }
                    }
                });
            },

            createStory: function(title, imageId, echoId) {
                alert("createStory: title = " + title + ", imageId = " + imageId + ", echoId = " + echoId);
                $.ajax({
                    url: Echoed.urls.api + "/story",
                    type: "POST",
                    xhrFields: {
                        withCredentials: true
                    },
                    dataType: 'json',
                    data: {
                        title: title,
                        imageId: imageId,
                        echoId: echoId
                    },
                    success: function(createStoryData) {
                        var storyId = createStoryData.id;
                        var imageId = createStoryData.image.id;
                        storyTest.createStoryData = createStoryData;
                        storyTest.updateStory(storyId, imageId);
                    }
                });
            },

            updateStory: function(storyId, imageId) {
                alert("updateStory: storyId = " + storyId + ", imageId = " + imageId);
                $.ajax({
                    url: Echoed.urls.api + "/story/" + storyId,
                    type: "PUT",
                    xhrFields: {
                        withCredentials: true
                    },
                    dataType: 'json',
                    data: {
                        title: "My awesome updated story title",
                        imageId: imageId
                    },
                    success: function(updatedStoryData) {
                        var storyId = updatedStoryData.id;
                        var chapterTitle = storyTest.initStoryData.storyPrompts.prompts[1];
                        storyTest.updatedStoryData = updatedStoryData;
                        storyTest.createChapter(storyId, chapterTitle);
                    }
                });
            },

            createChapter: function(storyId, chapterTitle) {
                alert("createChapter: storyId = " + storyId + ", chapterTitle = " + chapterTitle);
                $.ajax({
                    url: Echoed.urls.api + "/story/" + storyId + "/chapter",
                    type: "POST",
                    xhrFields: {
                        withCredentials: true
                    },
                    dataType: 'json',
                    processData: false,
                    contentType: "application/json",
                    data: JSON.stringify({
                            title: chapterTitle,
                            text: "My awesome chapter text",
                            imageIds: ["01636a85-dd6a-452d-86ce-8c0b62a80708", "ce057ad4-4c01-4470-8823-987c08028746"]
                    }),
                    success: function(createChapterData) {
                        var storyId = createChapterData.chapter.storyId;
                        var chapterId = createChapterData.chapter.id;
                        var imageId = createChapterData.chapterImages[0].image.id
                        storyTest.createChapterData = createChapterData;
                        storyTest.updateChapter(storyId, chapterId, imageId);
                    }
                });
            },

            updateChapter: function(storyId, chapterId, imageId) {
                alert("updateChapter: storyId = " + storyId + ", chapterId = " + chapterId + ", imageId = " + imageId);
                $.ajax({
                    url: Echoed.urls.api + "/story/" + storyId + "/chapter/" + chapterId,
                    type: "PUT",
                    xhrFields: {
                        withCredentials: true
                    },
                    dataType: 'json',
                    processData: false,
                    contentType: "application/json",
                    data: JSON.stringify({
                            title: "My awesome updated chapter title",
                            text: "My awesome updated chapter text",
                            imageIds: [imageId, "01636a85-dd6a-452d-86ce-8c0b62a80708", "ce057ad4-4c01-4470-8823-987c08028746"]
                    }),
                    success: function(updatedChapterData) {
                        var storyId = updatedChapterData.chapter.storyId;
                        var chapterId = updatedChapterData.chapter.id;
                        storyTest.updatedChapterData = updatedChapterData;
                        storyTest.createComment(storyId, chapterId);
                    }
                });
            },

            createComment: function(storyId, chapterId) {
                alert("createComment: storyId = " + storyId + ", chapterId = " + chapterId);
                $.ajax({
                    url: Echoed.urls.api + "/story/" + storyId + "/chapter/" + chapterId + "/comment",
                    type: "POST",
                    xhrFields: {
                        withCredentials: true
                    },
                    dataType: 'json',
                    data: {
                        text: "My awesome comment"
                    },
                    success: function(createCommentData) {
                        var storyId = createCommentData.storyId;
                        var chapterId = createCommentData.chapterId;
                        var parentCommentId = createCommentData.id;
                        storyTest.createCommentData = createCommentData;
                        storyTest.replyComment(storyId, chapterId, parentCommentId);
                    }
                });

            },

            replyComment: function(storyId, chapterId, parentCommentId) {
                alert("replyComment: storyId = " + storyId + ", chapterId = " + chapterId + ", parentCommentId = " + parentCommentId);
                $.ajax({
                    url: Echoed.urls.api + "/story/" + storyId + "/chapter/" + chapterId + "/comment",
                    type: "POST",
                    xhrFields: {
                        withCredentials: true
                    },
                    dataType: 'json',
                    data: {
                        parentCommentId: parentCommentId,
                        text: "My awesome reply comment"
                    },
                    success: function(replyCommentData) {
                        alert("CONGRATS!!!! You've completed the tests successfully - last response is: " + JSON.stringify(replyCommentData));
                        storyTest.replyCommentData = replyCommentData;
                    }
                });
            }
        };
        storyTest.initStory(id);
    },
    friends: function() {
        this.EvAg.trigger('friends/init');
        this.EvAg.trigger("page/change","friends");
        this.EvAg.trigger('filter/hide');
        this.page = "Friends";
        _gaq.push(['_trackPageview', this.page]);

    },
    friendsExhibit: function(id, filter){
        var newPage = "Friends/Exhibit/" + id;
        if(this.page != newPage){
            this.EvAg.trigger('exhibit/init', { Filter: filter, Type: 'friend', Id: id});
            this.page = newPage;
            _gaq.push(['_trackPageview', this.page]);

        } else {
            this.EvAg.trigger("filter/change",filter);
        }
        this.EvAg.trigger("page/change","friends");
    }
});

Echoed.Views.Components.Field = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this, 'render', 'load','updateEndpoint','openPhotoDialog','submitInitStory','loadChapterTemplate','loadChapterHelper','submitChapter');
        this.element = $(options.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind("field/show", this.load);
        this.EvAg.bind("focus/update", this.updateEndpoint);
        this.prompts = [];
    },
    events: {
        "click .field-close" : "close",
        "click .field-photo" : "openPhotoDialog",
        "click .field-submit" : "submitInitStory",
        "click input[type=text]" : "loadChapterHelper",
        "click .chapter-submit": "submitChapter"
    },
    updateEndpoint: function(options){
        var self = this;
        self.endpointBase = Echoed.urls.api + '/api/partner/' + options.partner + '/' + options.product;
    },
    openPhotoDialog: function(options){
        var self = this;
    },
    load: function(id){
        var self = this;
        $.ajax({
            url: Echoed.urls.api + "/story?echoId=" + id,
            type: "GET",
            xhrFields: {
                withCredentials: true
            },
            dataType: 'json',
            success: function(initStoryData){
                self.template = _.template($('#templates-components-story-input').html());
                self.titleImage = initStoryData.echo.image.sizedUrl;
                self.imageId = initStoryData.echo.image.id;
                self.storyPrompts = initStoryData.storyPrompts.prompts;
                self.partnerName = initStoryData.partner.name;
                self.partnerId = initStoryData.partner.partnerId;
                self.productName = initStoryData.echo.productName;
                self.echoId = initStoryData.echo.id;
                self.render();
            }
        });
    },
    submitInitStory: function(){
        var self = this;
        self.title = self.element.find("#story-name").val();
        var imageId = self.imageId;
        $.ajax({
            url: Echoed.urls.api + "/story",
            type: "POST",
            xhrFields: {
                withCredentials: true
            },
            dataType: 'json',
            data: {
                title: self.title,
                echoId: self.echoId,
                imageId: imageId
            },
            success: function(data){
                self.storyId = data.id;
                self.loadChapterTemplate();
            }
        });
    },
    loadChapterHelper: function(){
        var self = this;
        self.chapterHelper.fadeIn();
    },
    loadChapterTemplate: function(){
        var self = this;
        self.template = _.template($('#templates-components-story-edit').html());
        self.element.html(self.template);
        self.chapterHelper = self.element.find(".chapter-title-helper");
        self.chapterHelper.append($("<div class='story-title'>Things to talk about</div>"));
        $.each(self.storyPrompts, function(index, prompt){
            self.chapterHelper.append($("<div class='s-e-b-r-i'></div>").append(prompt))
        });
        $("#story-title").html(self.title);
        $("#story-partner").html(self.partnerName);
        $("#chapter-title").val(self.storyPrompts[0]);
        $("#story-title-photo").attr("src", self.titleImage);
        var chapterPhotos = self.element.find(".thumbnails");
        self.chapterImages = [];
        self.chapterTitle = "";
        self.chapterText = "";
        var uploader = new qq.FileUploader({
            element: document.getElementsByClassName('photo-upload')[0],
            action: '/image',
            debug: true,
            allowedExtensions: ['jpg', 'jpeg', 'png', 'gif'],
            onSubmit: function(id, fileName) {

            },
            onComplete: function(id, fileName, response) {
                var thumbDiv = $('<div></div>').addClass("thumb");
                var photo = $('<img height="100px" />').attr('src', response.url);
                thumbDiv.append(photo).prependTo(chapterPhotos);
                self.chapterImages.push(response.id);
            }
        });
        self.chapterHelper.fadeIn();
    },
    submitChapter: function(){
        var self = this;
        self.chapterTitle = $('#chapter-title').val();
        self.chapterText = $('#chapter-text').val();
        $.ajax({
            url: Echoed.urls.api + "/story/" + self.storyId + "/chapter",
            type: "POST",
            xhrFields: {
                withCredentials: true
            },
            dataType: 'json',
            processData: false,
            contentType: "application/json",
            data: JSON.stringify({
                title: self.chapterTitle,
                text: self.chapterText,
                imageIds: self.chapterImages
            }),
            success: function(createChapterData) {
                alert("Successfully created chapter, response is: " + JSON.stringify(createChapterData))
            }
        });

    },
    render: function(){
        var self = this;
        self.element.empty();
        self.element.html(self.template);
        self.element.find("#thumb").attr("src", self.titleImage);
        self.element.find("#partner-name").val(self.partnerName);
        self.element.find("#story-name").val(self.productName);
        self.element.fadeIn();
        self.EvAg.trigger('fade/show');
    },
    close: function(){
        var self = this;
        self.element.fadeOut();
        self.element.empty();
        self.EvAg.trigger('fade/hide');
    }
});

Echoed.Views.Components.Transition = Backbone.View.extend({
    el: '#transition',
    initialize: function(options){
        _.bindAll(this,'render');
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.bind('transition/show', this.render);
    },
    render: function(options){
        var self = this;
        var top = options.top;
        var left = options.left;
        var height = options.height;
        var width = options.width;
        self.element.css({
        });
    }
});

Echoed.Views.Components.InfiniteScroll = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'triggerScroll', 'lock', 'unlock', 'on', 'off','scrollUp');
        this.EvAg = options.EvAg;
        this.EvAg.bind("triggerInfiniteScroll", this.triggerScroll);
        this.EvAg.bind("infiniteScroll/lock", this.lock);
        this.EvAg.bind("infiniteScroll/unlock", this.unlock);
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
        _.bindAll(this,'render','filterProducts','next','init','complete','relayout', 'addProducts','addTitle');
        this.EvAg = options.EvAg;
        this.EvAg.bind('filter/change', this.filterProducts);
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

        if(!options.Filter) self.filter = '*';
        else self.filter = "." + options.Filter;
        self.contentDescription = "";
        self.showDate = false;
        switch(options.Type){
            case "friend":
                self.jsonUrl = Echoed.urls.api + "/api/user/" + options.Id;
                self.baseUrl = "#friends/exhibit/" + options.Id + "/";
                self.contentTitle = "Your Friends";
                self.id = "friends";
                self.nextInt = 1;
                break;
            case "partners":
                self.jsonUrl = Echoed.urls.api + "/api/partner/" + options.partnerId;
                self.baseUrl = "#partners/" + options.partnerId + "/";
                self.contentTitle = options.Name;
                self.id = "partners";
                self.showDate = 1;
                self.nextInt = 1;
                break;
            case "explore":
                self.jsonUrl = Echoed.urls.api + "/api/me/feed";
                self.baseUrl = "#explore/";
                self.contentTitle = "Just Purchased";
                self.contentDescription = "See all the purchased products at our partners as they're bought ";
                self.id= "explore";
                self.nextInt = 1;
                break;
            case "explore/friends":
                self.jsonUrl = Echoed.urls.api + "/api/me/feed/friends";
                self.baseUrl = "#exploref/";
                self.id = "explore/friends";
                self.nextInt = 1;
                break;
            case "exhibit":
                self.jsonUrl = Echoed.urls.api + "/api/me/exhibit";
                self.baseUrl = "#me/";
                self.contentTitle = "Me";
                self.contentDescription = "All the products you've shared and the rewards you've earned";
                self.id = null;
                self.nextInt = 1;
                break;
            case "story":
                self.jsonUrl = Echoed.urls.api + "/api/story/" + options.storyId;
                self.id = "story";
                break;
        }
        self.EvAg.trigger('filter/init', self.baseUrl);
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

        self.EvAg.trigger("filter/change",self.filter);


        if(data.partner){
            self.contentDescription = "The recent products purchased at " + data.partner.name;
            self.addTitle(data.partner.name, data.partner.logo);
        } else if (self.id == "friends") {
            self.contentDescription = "Products purchased and shared by " + data.echoedUserName;
            self.addTitle(data.echoedUserName);
        } else if (self.id != "story"){
            self.addTitle(self.contentTitle);
        }
        if(data.echoes){
            if(data.echoes.length > 0){
                self.addProducts(data);
            }
            else{
                self.nextInt = null;
                self.EvAg.trigger("infiniteScroll/unlock");
            }
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
        if(!filter) selector = '*';
        else selector = "." + encodeURIComponent(filter);
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
                        self.EvAg.trigger("infiniteScroll/unlock");
                    }
                }
            });
        }
    },
    addTitle: function(title, image){
        var self = this;
        var titleDiv = $('<div></div>').addClass('item_wrap').addClass('no_filter').attr("id","title");
        titleDiv.append(title);
        titleDiv.append($('<div></div>').addClass('title-description').append(self.contentDescription));
        self.exhibit.isotope('insert', titleDiv);
    },
    complete: function(){
        var self = this;
    },
    addStory: function(data){
        var self = this;
        var storyDiv = $('<div></div>');
        var storyComponent = new Echoed.Views.Components.Story({el : storyDiv, data: data});
        self.exhibit.isotope('insert',storyDiv);
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
        this.element.append($("<div echoId='9b6c4353-72ea-4595-bb18-e979c0c0a721' class='action-button'>Tell a Story</div>"));
    },
    click: function(e){
        var self = this;
        var target = $(e.target);
        var echoId = target.attr("echoId");
        self.EvAg.trigger("field/show", echoId);
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
                        var  a = $('<a></a>').attr("href","#friends/exhibit/" + friend.toEchoedUserId);
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
        _.bindAll(this,'render');
        this.el = options.el;
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.state = 0;
        this.render(options.data);
    },
    render: function(){
        var template = _.template($('#templates-components-story').html());
        var self = this;
        self.element.addClass("item_wrap");
        this.element.html(template);
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
        var imageUrl =   this.model.get("image").preferredUrl;
        var imageWidth = this.model.get("image").preferredWidth;
        var imageHeight = this.model.get("image").preferredHeight;
        this.el.attr("date", this.model.get("echoBoughtOn"));
        this.el.attr("productId", this.model.get("echoProductId"));
        this.el.attr("partnerHandle", this.model.get("partnerHandle"));
        this.el.attr("partnerId", this.model.get("partnerId"));
        this.el.attr("id", this.model.get("echoId"));
        this.el.addClass("item_wrap").addClass('Brand-' + encodeURIComponent(this.model.get("echoBrand"))).addClass('Browse-' + encodeURIComponent(this.model.get("echoCategory"))).html(template).attr("href", landingUrl);
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
        }
        img.attr('src', imageUrl);
        if (imageWidth > 230) {
            imageHeight = 230 / imageWidth * imageHeight;
            imageWidth = 230;
        }
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
        if(this.el.attr("partnerHandle"))
            window.location.hash = "#partners/" + this.el.attr('partnerHandle');
        else
            window.location.hash = "#partners/" + this.el.attr('partnerId');
    },
    click: function(e){
        var self = this;
        var href = this.el.attr("href");
        window.open(href);
    }
});
