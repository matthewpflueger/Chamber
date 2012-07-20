var EventAggregator = _.extend({}, Backbone.Events);

Echoed = {
    Views: {
        Pages:{},
        Components:{}
    },
    Models:{},
    Collections:{},
    AjaxFactory: function(params){
        var defaultParams = {
            type: "GET",
            xhrFields: {
                withCredentials: true
            },
            dataType: 'json',
            cache: false,
            success: function(data){
            }
        };
        for(var index in params){
            defaultParams[index] = params[index];
        }
        return function(){
            $.ajax(defaultParams);
        };
    },
    getImageSizing: function(image, width){
        return {
            height: image.preferredHeight / image.preferredWidth * width,
            width: width
        }
    },
    getProfilePhotoUrl: function(echoedUser){
        if(echoedUser.facebookId !== null){
            return "http://graph.facebook.com/" + echoedUser.facebookId + "/picture";
        } else if(echoedUser.twitterId !== null) {
            return "http://api.twitter.com/1/users/profile_image/" + echoedUser.twitterId;
        }
        return "";
    },
    getFacebookLoginUrl: function(hash){
        return Echoed.facebookLogin.head +
            encodeURIComponent(Echoed.facebookLogin.redirect + encodeURIComponent(hash)) +
            Echoed.facebookLogin.tail;
    },
    getTwitterLoginUrl: function(hash){
        return Echoed.twitterUrl + encodeURIComponent(hash);
    },
    isUrl: function(s){
        //var regexp = /(\w+:{3,3}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
        var regexp =/(http:\/\/|https:\/\/|www)(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
        return regexp.test(s);
    },
    makeUrl: function(s){
        if(s.indexOf("http") === -1){
            s = "http://" + s;
        }
        return s;
    },
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
        var title = new Echoed.Views.Components.Title({ el: '#title', EvAg: EventAggregator });
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
        $("html,body").addClass("noScroll");
        this.element.fadeIn();
    },
    hide: function(){
        this.element.fadeOut();
        $("html,body").removeClass("noScroll");
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
        _gaq.push(['_trackPageview', window.location.hash]);

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
        _.bindAll(this, 'render', 'unload', 'load', 'loadStoryTemplate', 'submitInitStory', 'loadChapterTemplate', 'cancelChapter','submitChapter', 'storyEditClick', 'removeChapterThumb');
        this.element = $(options.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind("field/show", this.load);
        this.locked = false;
        this.prompts = [];
    },
    events: {
        "click .field-close" : "close",
        "click .field-submit" : "submitInitStory",
        "click .chapter-submit": "submitChapter",
        "click .chapter-cancel": "cancelChapter",
        "click .chapter-thumb-x": "removeChapterThumb",
        "click .story-summary-submit": "storySummarySubmit",
        "click .story-summary-chapter-edit": "storyEditClick"

    },
    load: function(id, type){
        var self = this;
        var jsonUrl =  Echoed.urls.api + "/story";
        var loadData = {};
        self.data = {};

        switch(type){
            case "story":
                loadData.storyId = id;
                self.storyId =id;
                break;
            case "echo":
                loadData.echoId = id;
                self.echoId = id;
                break;
            case "partner":
                loadData.partnerId = id;
                self.partnerId = id;
                break;
        }
        if(Echoed.echoedUser){
            Echoed.AjaxFactory({
                url: jsonUrl,
                data: loadData,
                dataType: 'jsonp',
                success: function(initStoryData){
                    self.data = initStoryData;
                    self.render();
                }
            })();
        } else {
            self.data = {};
            if(type == "partner"){
                Echoed.AjaxFactory({
                    url: Echoed.urls.api + "/api/partner/" + id,
                    dataType: 'jsonp',
                    success: function(data){
                        self.renderLogin(data);
                    }
                })();
            } else {
                self.renderLogin();
            }
        }
    },
    submitInitStory: function(){
        var self = this;
        if(self.locked === false) {
            var title = $.trim(self.element.find("#story-name").val());
            var productFrom = $.trim($('#story-from').val());
            var storyData = {
                title: title,
                imageId: self.data.imageId,
                productInfo: productFrom
            };

            if(self.data.echo) {
                storyData.echoId = self.data.echo.id;
            }
            else if(self.partnerId) {
                storyData.partnerId = self.partnerId;
            }

            if(!self.data.imageId){
                alert("Please select a photo for the product");
            } else if(title === ""){
                alert("Please title your product story");
            } else if(productFrom === "") {
                alert("Please enter where the product is from");
            } else {
                self.locked = true;
                Echoed.AjaxFactory({
                    url: Echoed.urls.api + "/story",
                    type: 'POST',
                    data: storyData,
                    success: function(createStoryResponse){
                        self.load(createStoryResponse.id, "story");
                        self.locked = false;
                    }
                })();
            }
        }
    },
    loadStorySummary: function(){
        var self = this;
        var template = _.template($('#templates-components-story-summary').html());
        self.element.html(template);

        self.element.find(".story-preview-title").html(self.data.storyFull.story.title);
        self.element.find(".story-preview-from").html(self.data.storyFull.story.productInfo);
        self.element.find(".story-preview-by").html(self.data.storyFull.echoedUser.name);
        $("#story-preview-photo").attr("src", self.data.storyFull.story.image.preferredUrl);

        var count = 0;
        self.element.chapters = self.element.find('.story-summary-body');
        $.each(self.data.storyFull.chapters, function(index, chapter){
            count = count + 1;
            var chapterDiv = $('<div class="story-summary-chapter"></div>').addClass("clearfix");
            var chapterEdit = $("<div class='story-summary-chapter-row'></div>").append($('<div class="story-summary-chapter-edit"></div>').html("Edit").attr("chapterId", index));
            var chapterTitle = $("<div class='story-summary-chapter-row'></div>").append($('<div class="story-summary-chapter-title"></div>').html("<strong>Topic: </strong>" + chapter.title));
            var chapterDescription = $("<div class='story-summary-chapter-row'></div>").append($('<div class="story-summary-chapter-description"></div>').html("<strong>Description: </strong>" +chapter.text));
            var chapterPhotos = $('<div class="story-summary-chapter-photo-container"></div>');
            var chapterPhotosRow = $("<div class='story-summary-chapter-row'></div>").append($('<label>Photos: </label>')).append(chapterPhotos);
            $.each(self.data.storyFull.chapterImages, function(index, chapterImage){
                if(chapterImage.chapterId === chapter.id){
                    var chapterImg = $('<img class="story-summary-chapter-photo"/>').attr("height", 50).attr("src",chapterImage.image.preferredUrl);
                    chapterPhotos.append(chapterImg);
                }
            });
            chapterDiv.append(chapterEdit).append(chapterTitle).append(chapterDescription).append(chapterPhotosRow);
            self.element.chapters.append(chapterDiv);
        });
        self.show();
    },
    storySummarySubmit: function(e){
        var self = this;
        var target = $(e.target);
        var nextAction = target.attr("act");
        switch(nextAction){
            case "finish":
                self.unload(function(){
                    window.location.hash = "#story/" + self.data.storyFull.story.id;
                });
                break;
            case "add":
                self.loadChapterTemplate();
                break;
        }
    },
    storyEditClick: function(e){
        var self = this;
        var domNode = $(e.target);
        var chapterId = domNode.attr("chapterId");
        self.loadChapterTemplate(chapterId);
    },
    removeChapterThumb: function(e){
        var self = this;
        var target = $(e.currentTarget).parent();
        var index = target.attr("index");
        self.currentChapter.images.splice(index, 1);
        target.remove();
    },
    loadChapterTemplate: function(chapterIndex){
        var self = this;
        self.template = _.template($('#templates-components-story-edit').html());
        self.element.html(self.template);
        self.element.find(".story-preview-title").html(self.data.storyFull.story.title);
        self.element.find(".story-preview-from").html(self.data.storyFull.story.productInfo);
        self.element.find(".story-preview-by").html(self.data.storyFull.echoedUser.name);
        $("#story-preview-photo").attr("src", self.data.storyFull.story.image.preferredUrl);
        var chapterPhotos = self.element.find(".thumbnails");
        self.currentChapter = {
            images: [],
            title: "",
            text: ""
        };
        var selectOptions = {
            optionsArray : [],
            el : "#chapter-title"
        };

        if(chapterIndex !== undefined){
            var chapterId = self.data.storyFull.chapters[chapterIndex].id;
            self.currentChapter.title = self.data.storyFull.chapters[chapterIndex].title;
            selectOptions.currentTitle = self.currentChapter.title;
            self.currentChapter.text = self.data.storyFull.chapters[chapterIndex].text;
            selectOptions.optionsArray.push(self.currentChapter.title);
            self.currentChapter.id = chapterId;
            $("#chapter-title").val(self.data.storyFull.chapters[chapterIndex].title);
            $("#chapter-text").val(self.data.storyFull.chapters[chapterIndex].text);
            $.each(self.data.storyFull.chapterImages, function(index, chapterImage){
                if(chapterImage.chapterId === chapterId){
                    var thumbDiv = $('<div></div>').addClass("thumb").addClass('chapter-thumb').attr("index", index);
                    var thumbX = $('<img />').addClass('chapter-thumb-x').attr('src', Echoed.urls.images + "/btn_close_x_black.png").css({
                        "height" : 20,
                        "width" : 20
                    });
                    thumbDiv.append(thumbX);
                    var photo = $('<img />').attr('src', chapterImage.image.preferredUrl).css(Echoed.getImageSizing(chapterImage, 75));
                    thumbDiv.append(photo).appendTo(chapterPhotos).fadeIn();
                    self.currentChapter.images.push(chapterImage.image.id);
                }
            });
        }
        $.each(self.data.storyPrompts.prompts, function(index, prompt){
            var inChapters = false;
            $.each(self.data.storyFull.chapters, function(index, chapter){
                if(prompt === chapter.title){
                    inChapters = true;
                }
            });
            if(inChapters === false){
                selectOptions.optionsArray.push(prompt)
            }
        });
        $("#chapter-text").expandingTextarea();

        self.select = new Echoed.Views.Components.Select(selectOptions);
        var uploader = new qq.FileUploader({
            element: document.getElementsByClassName('photo-upload')[0],
            action: '/image',
            debug: true,
            allowedExtensions: ['jpg', 'jpeg', 'png', 'gif'],
            onProgress: function(id, fileName, loaded, total){
            },
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
        self.currentChapter.title = $.trim(self.select.val());
        self.currentChapter.text = $.trim($('#chapter-text').val());
        if(self.currentChapter.title === ""){
            alert("Please select or write a topic.");
        } else if(self.currentChapter.text === ""){
            alert("You must have some text for your topic. Even a single sentence is enough!");
        } else {
            if(self.locked === false) {
                self.locked = true;
                if(self.currentChapter.id !== undefined){

                    Echoed.AjaxFactory({
                        url: Echoed.urls.api + "/story/" + self.data.storyFull.story.id + "/chapter/" + self.currentChapter.id,
                        type: "PUT",
                        processData: false,
                        contentType: "application/json",
                        data: JSON.stringify({
                            title: self.currentChapter.title,
                            text: self.currentChapter.text,
                            imageIds: self.currentChapter.images
                        }),
                        success: function(chapterSubmitResponse) {
                            self.locked = false;
                            self.load(self.data.storyFull.story.id, "story");
                        }
                    })();
                } else {
                    Echoed.AjaxFactory({
                        url: Echoed.urls.api + "/story/" + self.data.storyFull.story.id + "/chapter",
                        type: "POST",
                        processData: false,
                        contentType: "application/json",
                        data: JSON.stringify({
                            title: self.currentChapter.title,
                            text: self.currentChapter.text,
                            imageIds: self.currentChapter.images
                        }),
                        success: function(chapterSubmitResponse) {
                            self.locked = false;
                            self.load(self.data.storyFull.story.id, "story");
                        }
                    })();
                }
            }
        }
    },
    cancelChapter: function(){
        var self = this;
        if(self.data.storyFull.chapters.length > 0){
            self.load(self.data.storyFull.story.id, "story");
        } else{
            self.close();
        }

    },
    unload: function(callback){
        var self = this;
        self.EvAg.trigger('fade/hide');
        self.element.fadeOut(function(){
            callback();
            self.element.empty();
            self.data = {};
        });
    },
    renderLogin: function(data){
        var self = this;
        self.template = _.template($('#templates-components-story-login').html());
        self.element.html(self.template).addClass('small');
        $("#field-fb-login").attr("href", Echoed.getFacebookLoginUrl(window.location.hash));
        $("#field-tw-login").attr("href", Echoed.getTwitterLoginUrl(window.location.hash));
        var body = self.element.find(".field-login-body");
        if(data){
            var bodyText = data.partner.name + " wants to hear your story. Share your story and have it featured on the " + data.partner.name + " page.";
            var bodyTextNode = $('<div class="field-login-body-text"></div>').append(bodyText);
            body.append(bodyTextNode);
        }

        self.show();
    },
    render: function(){
        var self = this;
        self.element.empty();
        if(self.data.storyFull) {
            if(self.data.storyFull.chapters.length > 0) {
                self.loadStorySummary();
            } else {
                self.loadChapterTemplate();
            }
        } else {
            self.loadStoryTemplate();
        }
    },
    loadStoryTemplate: function(){
        var self = this;
        self.template = _.template($('#templates-components-story-input').html());
        self.element.html(self.template);
        self.data.imageId = null;
        if (self.data.partner && self.data.partner.name != "Echoed"){
            $("#story-from").val(self.data.partner.name).attr("readonly",true);
            self.element.find('.field-title').html("Share Your " + self.data.partner.name + " Story");
        }
        if(self.data.echo){
            $("#field-photo").attr("src", self.data.echo.image.preferredUrl);
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
        self.element.fadeIn().css({
            "margin-left" : -(self.element.width()/2)
        });
        $("#story-name").focus();
    },
    close: function(){
        var self = this;
        self.element.fadeOut().empty();
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

Echoed.Views.Pages.Exhibit = Backbone.View.extend({
    el: '#content',
    initialize: function(options){
        _.bindAll(this,'render','next','init','relayout', 'addProducts', 'nextStory', 'previousStory');
        this.EvAg = options.EvAg;
        this.EvAg.bind('exhibit/relayout', this.relayout);
        this.EvAg.bind('exhibit/init', this.init);
        this.EvAg.bind('infiniteScroll', this.next);
        this.EvAg.bind('exhibit/story/next', this.nextStory);
        this.EvAg.bind('exhibit/story/previous', this.previousStory);
        this.element = $(this.el);
        this.exhibit = $('#exhibit');
    },
    init: function(options){
        var self = this;
        self.exhibit.isotopeOn = true;
        self.personal = false;
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
                self.id = "partner/" + options.partnerId;
                self.nextInt = 1;
                break;
            case "explore":
                self.jsonUrl = Echoed.urls.api + "/api/me/feed";
                self.contentTitle = "Community";
                self.contentDescription ="";
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
                self.id = "exhibit";
                self.contentTitle = "My Stories";
                self.personal = true;
                self.nextInt = 1;
                break;
        }
        Echoed.AjaxFactory({
            url: self.jsonUrl,
            dataType: 'json',
            success: function(data){
                self.stories = {
                    array: [],
                    hash: {}
                };
                self.render(data);
                self.EvAg.trigger("infiniteScroll/unlock");
            }
        })();
    },
    nextStory: function(storyId){
        var self = this;
        var index = self.stories.hash[storyId];
        if((index + 1) >= self.stories.array.length){
            self.next();
        }
        if((index +1 ) < self.stories.array.length){
            window.location.hash = "#story/" + self.stories.array[index + 1];
        }
    },
    previousStory: function(storyId){
        var self = this;
        var index = self.stories.hash[storyId];
        if(index> 0){
            window.location.hash = "#story/" + self.stories.array[index - 1];
        }
    },
    render: function(data){
        var self = this;
        if (self.exhibit.isotopeOn) self.exhibit.isotope("destroy");
        self.exhibit.empty();
        self.exhibit.isotope({
            itemSelector: '.item_wrap,.no_filter',
            masonry:{
                columnWidth: 5
            }
        });
        if(data.partner){
            self.EvAg.trigger("title/update", { title: data.partner.name, description: self.contentDescription });
        } else if (self.id === "friends") {
            self.EvAg.trigger("title/update", { title: data.echoedUser.name, description: self.contentDescription });
        } else if (self.id !== "story"){
            self.EvAg.trigger("title/update", { title: self.contentTitle, description: self.contentDescription });
        }
        if(!Echoed.echoedUser) self.addLogin();

        self.addStories(data);
        self.addProducts(data);

        if(data.stories || data.echoes){
            self.EvAg.trigger('infiniteScroll/on');
            self.EvAg.trigger('infiniteScroll/unlock');
            self.nextInt++;
        } else {
            self.nextInt = null;
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
            var url = self.jsonUrl + "?page=" + (self.nextInt - 1);
            Echoed.AjaxFactory({
                url: url,
                success: function(data){
                    self.addProducts(data);
                    self.addStories(data);
                    if(data.stories.length > 0){
                        self.nextInt++;
                    } else {
                        self.nextInt = null;
                        self.EvAg.trigger("infiniteScroll/unlock");
                    }
                }
            })();
        }
    },
    addLogin: function(){
        var self = this;
        var loginDiv = $('<div></div>').addClass('item_wrap');
        var template = _.template($("#templates-components-login").html());
        loginDiv.html(template);
        loginDiv.find("#facebookLogin").attr("href", Echoed.getFacebookLoginUrl(window.location.hash));
        loginDiv.find("#twitterLogin").attr("href", Echoed.getTwitterLoginUrl(window.location.hash));
        self.exhibit.isotope('insert', loginDiv)
    },
    addStories: function(data){
        var self = this;
        var storiesFragment = $('<div></div>');
        if(data.stories){
            $.each(data.stories, function(index, story){
                if(story.chapters.length > 0 || self.personal == true){
                    self.stories.hash[story.id] = self.stories.array.length;
                    self.stories.array.push(story.id);
                    var storyDiv = $('<div></div>').addClass('item_wrap');
                    var storyComponent = new Echoed.Views.Components.StoryBrief({el : storyDiv, data: story, EvAg: self.EvAg, Personal: self.personal});
                    if(story.story.image.originalUrl !== null){
                        storiesFragment.append(storyDiv);
                    } else {
                        storyDiv.imagesLoaded(function(){
                            self.exhibit.isotope('insert', storyDiv);
                        });
                    }
                }
            });
            self.exhibit.isotope('insert', storiesFragment.children(), function(){
                self.EvAg.trigger('infiniteScroll/unlock');
            });
        }
    },
    addProducts: function(data){
        var self = this;
        var productsFragment = $('<div></div>');
        if(data.echoes){
            $.each(data.echoes, function(index, product){
                var productDiv = $('<div></div>');
                var productModel = new Echoed.Models.Product(product);
                var productComponent = new Echoed.Views.Components.Product({el:productDiv, model:productModel, EvAg: self.EvAg, Personal: self.personal });
                productsFragment.append(productDiv);
            });
            self.exhibit.isotope('insert',productsFragment.children(), function(){
                self.EvAg.trigger('infiniteScroll/unlock');
            });
        }
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
        this.element.empty();
        this.element.append($("<div class='action-button'>Share a Story</div>"));
    },
    click: function(e){
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
        self.EvAg.trigger('title/update', { title: "My Friends", description: "All Your Friends On Echoed" });
        self.exhibit.empty();
        self.exhibit.isotope("destroy");
        var jsonUrl = Echoed.urls.api + "/api/me/friends";
        Echoed.AjaxFactory({
            url: jsonUrl,
            success: function(data){
                if(data.length > 0 ){
                    $.each(data, function(index,friend){
                        var friendImage = $('<div class="friend-img"></div>');
                        var friendText = $('<div class="friend-text"></div>').html(friend.name);
                        var  a = $('<a></a>').attr("href","#user/" + friend.toEchoedUserId);
                        $('<img />').attr("height","50px").attr("src",Echoed.getProfilePhotoUrl(friend)).appendTo(friendImage);
                        $('<div></div>').addClass("friend").append(friendImage).append(friendText).appendTo(a);
                        a.appendTo(self.exhibit);
                    });
                }
            }
        })();
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
        this.optionsArray = options.optionsArray;
        this.currentTitle = options.currentTitle;
        this.openState = false;
        this.default = "(Write Your Own Topic)";
        this.render();
    },
    events: {
        "mouseenter" : "open",
        "mouseleave" : "close",
        "click .field-question-label" : "click",
        "click .field-question-option" : "selectOption",
        "keyup :input": "keyPress"
    },
    val: function(){
        return this.input.val();
    },
    render: function(){
        var self = this;
        self.input = $("<input type='text'>");

        self.label = $("<div class='field-question-label'></div>");
        self.label.append(self.input);
        self.element.append(self.label);
        self.optionsList = $("<div class='field-question-options-list'></div>").css("display", "none");
        self.element.append(self.optionsList);
        $.each(self.optionsArray, function(index, option){
            self.options[index] = $("<div class='field-question-option'></div>").append(option);
            self.optionsList.append(self.options[index]);
        });
        self.options[self.optionsArray.length] = $("<div class='field-question-option'></div>").append(self.default);
        self.optionsList.append(self.options[self.optionsArray.length]);
        self.input.val(self.options[0].html());
    },
    keyPress: function(e){
        switch(e.keyCode){
            case 13:
                this.close();
        }
    },
    click: function(){
        var self = this;
        if(self.locked !== true){
            if(self.openState == false ){
                self.open();
            } else {
                self.openState = false;
                self.close();
            }
        }
    },
    selectOption: function(e){
        var target = $(e.target);
        this.input.val(target.html());
        this.close();
    },
    close: function(){
        this.openState = false;
        this.optionsList.hide();
        if(this.input.val() === this.default){
            this.input.val("");
            this.input.select();
        }
    },
    open: function(){
        this.openState = true;
        this.input.focus();
        this.input.select();
        this.optionsList.show();
    }
});

Echoed.Views.Components.Nav = Backbone.View.extend({
    el: "#header-nav",
    initialize: function(options){
        _.bindAll(this, 'click');
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.li = this.element.find('li');
    },
    events:{
        "click li": "click"
    },
    click: function(e){
        this.li.removeClass("current");
        $(e.target).addClass("current");
        window.location.hash = $(e.target).attr("href");
    }
});

Echoed.Views.Components.Story = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'render', 'load','createComment', 'renderImage', 'imageClick', 'nextImage','navClick');
        this.el = options.el;
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind('story/show', this.load);
        this.locked = false;
    },
    events: {
        "click .echo-s-h-close" : "close",
        "click .comment-submit": "createComment",
        "click .echo-s-b-thumbnail": "imageClick",
        "click .echo-s-b-item": "nextImage",
        "click .story-nav-button": "navClick",
        "click a": "close"
    },
    navClick: function(ev){
        var self = this;
        var target = $(ev.currentTarget);
        var action = target.attr("act");
        self.EvAg.trigger('exhibit/story/'+ action, self.data.story.id);
    },
    load: function(id){
        var self = this;
        Echoed.AjaxFactory({
            url: Echoed.urls.api + "/api/story/" + id,
            success: function(data){
                self.data = data;
                self.render();
            }
        })();
    },
    render: function(){
        var template = _.template($('#templates-components-story').html());
        var self = this;
        self.element.html(template);
        self.chapters = {
            array: [],
            hash: {}
        };

        $.each(self.data.chapters, function(index,chapter){
            var hash = {
                chapter: chapter,
                images: []
            };
            if(index === 0){
                hash.images.push(self.data.story.image);
            }
            $.each(self.data.chapterImages, function(index, chapterImage){
                if(chapterImage.chapterId === chapter.id) {
                    hash.images.push(chapterImage.image);
                }
            });
            if(hash.images.length === 0){
                hash.images.push(self.data.story.image);
            }
            self.chapters.array.push(hash);
            self.chapters.hash[chapter.id] = index;
        });
        self.currentChapterIndex = 0;
        self.currentImageIndex = 0;

        self.text = self.element.find('.echo-s-b-text');
        self.element.find('.echo-s-h-t-t').html(self.data.story.title);
        self.element.find('.echo-s-h-i-i').attr("src",self.data.story.image.storyUrl);
        self.gallery = self.element.find('.echo-s-b-gallery');
        self.userNode = self.element.find('.echo-s-h-t-n');
        var userLink = 'by <a href="#user/' + self.data.echoedUser.id + '">' + self.data.echoedUser.name + '</a><br/>';
        var fromLink = 'from ';
        if(self.data.story.partnerHandle !== "Echoed"){
            var p = self.data.story.partnerHandle ? self.data.story.partnerHandle : self.data.story.partnerId;
            fromLink = fromLink + '<a href="#partner/' + p + '">' + self.data.story.productInfo + '</a>';
        } else if (Echoed.isUrl(self.data.story.productInfo)){
            fromLink = fromLink + '<a target="_blank" href="' + Echoed.makeUrl(self.data.story.productInfo) + '">' + self.data.story.productInfo + '</a>';
        } else {
            fromLink = fromLink +  self.data.story.productInfo;
        }
        var userImage = $('<img />').attr("src", Echoed.getProfilePhotoUrl(self.data.echoedUser)).addClass("echo-s-h-t-n-i");
        self.userNode.append(userImage);

        self.itemNode = $("<div class='echo-s-b-item'></div>");
        self.itemImageContainer = $("<div class='echo-s-b-i-c'></div>");
        self.userTextNode = $("<div class='echo-s-h-t-n-t'></div>");
        self.userTextNode.append(userLink).append(fromLink).appendTo(self.userNode);
        self.img = $("<img />");
        self.itemNode.append(self.itemImageContainer.append(self.img)).appendTo(self.gallery);
        self.galleryNode = $("#echo-story-gallery");
        var chapterTitle = $("<div class='echo-s-b-t-t'></div>").append(self.data.chapters[0].title);
        var chapterText = $("<div class='echo-s-b-t-b'></div>").append(self.data.chapters[0].text.replace(/\n/g, '<br />'));
        self.text.append(chapterTitle).append(chapterText);
        self.renderGalleryNav();
        self.renderComments();
        self.renderChapter();

        self.EvAg.trigger('fade/show');
        self.element.css({
           "margin-left": -(self.element.width() / 2)
        });
        self.element.fadeIn();
    },
    renderGalleryNav: function(){
        var self = this;
        self.thumbnails = {};
        self.titles = [];
        self.galleryChapters = [];
        $.each(self.chapters.array, function(index, chapter){
            self.galleryChapters[index]=  $('<div></div>').addClass('echo-gallery-chapter');
            var title = $('<div></div>').addClass('echo-gallery-title').html(chapter.chapter.title);
            self.galleryChapters[index].append(title);
            self.galleryNode.append(self.galleryChapters[index]);
            $.each(chapter.images, function(index2, image){
                var thumbNailHash = index + "-" + index2;
                self.thumbnails[thumbNailHash] = $('<img />').addClass("echo-s-b-thumbnail").attr("index", thumbNailHash).attr("src", image.preferredUrl).css(Echoed.getImageSizing(image, 90));
                self.galleryChapters[index].append(self.thumbnails[thumbNailHash]);
            });
        });
    },
    nextImage: function(){
        var self = this;
        self.currentImageIndex++;
        if(self.currentImageIndex >= self.chapters.array[self.currentChapterIndex].images.length){
            self.nextChapter();
        } else {
            self.renderImage(self.currentImageIndex);
        }
    },
    nextChapter: function(){
        var self = this;
        self.currentChapterIndex++;
        self.currentImageIndex = 0;
        if(self.currentChapterIndex >= self.chapters.array.length){
            self.currentChapterIndex = 0;
        }
        self.renderChapter(self.currentChapterIndex);
    },
    imageClick: function(e){
        var self = this;
        var index = $(e.target).attr("index");
        self.currentChapterIndex = index.split("-")[0];
        self.currentImageIndex = index.split("-")[1];
        self.renderChapter();
    },
    renderChapter: function(){
        var self = this;
        var textArea = self.element.find('.echo-s-b-text');
        textArea.fadeOut(function(){
            self.element.find('.echo-s-b-t-t').html(self.chapters.array[self.currentChapterIndex].chapter.title);
            self.element.find('.echo-s-b-t-b').html('"' + self.chapters.array[self.currentChapterIndex].chapter.text.replace(/\n/g, '<br />') + '"');
            textArea.fadeIn();
        });

        self.galleryNode.find('.echo-gallery-chapter').removeClass("highlight");
        self.galleryChapters[self.currentChapterIndex].addClass("highlight");
        self.renderImage();
    },
    renderImage: function(){
        var self = this;
        var currentImage = self.chapters.array[self.currentChapterIndex].images[self.currentImageIndex];
        self.img.fadeOut();
        self.itemImageContainer.animate(
            Echoed.getImageSizing(currentImage, 400),
            function(){
                if(currentImage.storyUrl !== null){
                    self.img.attr('src', currentImage.storyUrl);
                } else {
                    self.img.attr('src', currentImage.originalUrl);
                }
                self.img.fadeIn();
        });

        self.galleryNode.find('.echo-s-b-thumbnail').removeClass("highlight");
        self.thumbnails[self.currentChapterIndex + "-" + self.currentImageIndex].addClass("highlight");
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
            var img = $('<img class="echo-s-c-l-c-u-i" />').attr("src", Echoed.getProfilePhotoUrl(comment.echoedUser)).attr("align", "absmiddle");
            img.prependTo(commentUserNode);
            var commentText = $('<div class="echo-s-c-l-c-t"></div>').append(comment.text.replace(/\n/g, '<br />'));
            var commentNode = $('<div class="echo-s-c-l-c"></div>').append(commentUserNode).append(commentText);
            commentListNode.append(commentNode);
        });
        if(Echoed.echoedUser) {
            self.element.find('.comment-submit').fadeIn();
        }
    },
    createComment: function(){
        var self = this;
        var storyId = self.data.story.id;
        var chapterId = self.data.chapters[0].id;
        var text = $.trim($("#echo-story-comment-ta").val());
        if(text === ""){
            alert("Please enter in a comment");
        } else if(self.locked !== true){
            self.locked = true;
            Echoed.AjaxFactory({
                url: Echoed.urls.api + "/story/" + storyId + "/chapter/" + chapterId + "/comment",
                type: "POST",
                data: {
                    text: text
                },
                success: function(createCommentData) {
                    self.locked = false;
                    self.data.comments.push(createCommentData);
                    self.renderComments();
                }
            })();
        }
    },
    close: function(){
        this.element.fadeOut();
        this.EvAg.trigger("fade/hide");
        this.EvAg.trigger("hash/reset");
    }
});

Echoed.Views.Components.StoryBrief = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'render','click','hideOverlay','showOverlay');
        this.el = options.el;
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.personal = options.Personal;
        this.data = options.data;
        this.render();
    },
    events: {
        "click .story-brief-overlay" : "click",
        "mouseenter": "showOverlay",
        "mouseleave": "hideOverlay"
    },
    render: function(){
        var self = this;
        var template = _.template($('#templates-components-story-brief').html());
        self.element.html(template);
        var imageNode = self.element.find(".story-brief-image");
        var textNode = self.element.find(".story-brief-text");
        var overlayNode = self.element.find(".story-brief-overlay-wrap");
        self.overlay = self.element.find(".story-brief-overlay");
        var image = self.data.story.image;
        if(image.preferredUrl !== null){
            imageNode.attr("src", image.preferredUrl).css(Echoed.getImageSizing(image, 260));
        } else {
            imageNode.attr("src", image.url)
        }
        if(self.personal === true ) {
            textNode.append("<strong>Story Title: </strong>"+ self.data.story.title + "<br/>");
            textNode.append("<strong># Chapters: </strong>" + self.data.chapters.length + "<br/>");
            textNode.append("<strong># Images: </strong>" + self.data.chapterImages.length + "<br/>");
            if(self.data.chapters.length === 0 ){
                var editButton = $('<div></div>').addClass("story-brief-overlay-edit-button").html("Complete Story");
                overlayNode.append(editButton);
                overlayNode.append("<br/>Complete your story by adding a chapter");
                self.overlay.fadeIn();
            } else {
                var editButton = $('<div></div>').addClass("story-brief-overlay-edit-button").html("Edit Story");
                overlayNode.append(editButton);
            }
        } else {
            textNode.append($("<img class='story-brief-text-user-image' height='35px' width='35px' align='absmiddle'/>").attr("src", Echoed.getProfilePhotoUrl(self.data.echoedUser)));
            textNode.append($("<div class='story-brief-text-title'></div>").append(self.data.story.title));
            textNode.append($("<div class='story-brief-text-by'></div>").append("Story by <a class='story-brief-text-user' href='#user/" + self.data.echoedUser.id + "'>" + self.data.echoedUser.name + "</a>"));

            var chapterText = self.data.chapters[0].text;
            var c  = chapterText.split(/[.!?]/)[0];
            c = c + chapterText.substr(c.length, 1); //Append Split Character
            textNode.prepend($("<div class='story-brief-text-quote'></div>").html('"' + c + '"'));
            overlayNode.html(self.data.story.title);
        }
        var dateString = self.data.story.updatedOn.toString();
        var elapsedString = timeElapsedString(timeStampStringToDate(dateString));

        var indicators = $("<div class='story-brief-indicator'></div>");

        indicators.append($("<span class='story-brief-indicator-sprite'></span>").addClass('sprite-comment'));
        indicators.append($("<span class='story-brief-indicator-value'></span>").append(self.data.comments.length));
        indicators.append($("<span class='story-brief-indicator-sprite'></span>").addClass('sprite-photo'));
        indicators.append($("<span class='story-brief-indicator-value'></span>").append(self.data.chapterImages.length + 1));
        indicators.append($("<span class='s-b-i-c'></span>").append(elapsedString));

        textNode.append(indicators);

        self.element.attr("id", self.data.story.id);
    },
    showOverlay: function(){
        this.overlay.fadeIn();
    },
    hideOverlay: function(){
        this.overlay.fadeOut();
    },
    click: function(){
        var self = this;
        if(this.personal) {
            window.location.hash = "#write/story/" + self.data.story.id;
        } else {
            var id = self.element.attr("id");
            window.location.hash = "#story/" + self.data.story.id;
        }
    }
});

Echoed.Views.Components.Title = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this, 'update');
        this.element = $(options.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind('title/update', this.update);
        this.titleText = $('#title-text');
        this.titleDescription = $('#title-description');
    },
    update: function(options){
        this.titleText.html(options.title);
    }
});

Echoed.Views.Components.Product = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this,'showOverlay','hideOverlay','click','clickPartner');
        this.el = options.el;
        this.EvAg = options.EvAg;
        this.personal = options.Personal;
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
        var image =   this.model.get("image");
        this.el.attr("date", this.model.get("echoBoughtOn"));
        this.el.attr("productId", this.model.get("echoProductId"));
        this.el.attr("partnerHandle", this.model.get("partnerHandle"));
        this.el.attr("partnerId", this.model.get("partnerId"));
        this.el.attr("id", this.model.get("echoId"));
        this.el.addClass("item_wrap").addClass('Browse-' + encodeURIComponent(this.model.get("echoCategory"))).html(template).attr("href", landingUrl);
        var hover = this.el.find(".item_hover_wrap");
        this.img = this.el.find("img");
        var text = this.el.find(".item_text");


        var boughtOnDate = new Date(this.model.get("echoBoughtOn"));
        text.append(timeElapsedString(boughtOnDate) + "<br/>");

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
        this.img.attr('src', image.preferredUrl).css(Echoed.getImageSizing(image, 260));

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
                this.img.addClass("open-echo");
            }
        }
        return this;
    },
    showOverlay: function(){
        this.img.addClass("highlight");
    },
    hideOverlay: function(){
        this.img.removeClass("highlight");
    },
    clickPartner: function(e){
        var id = this.el.attr("partnerHandle") ? this.el.attr("partnerHandle") : this.el.attr("partnerId");
        window.location.hash = "#partner/" + id;
    },
    click: function(e){
        if(this.personal){
            window.location.hash = "#write/echo/" + this.el.attr("id");
        } else {
            var href = this.el.attr("href");
            window.open(href);
        }
    }
});


function timeStampStringToDate(timestampString){
    var year = timestampString.substr(0,4);
    var month = timestampString.substr(4,2);
    var day = timestampString.substr(6,2);
    var hour = timestampString.substr(8,2);
    var minute = timestampString.substr(10,2);
    var second = timestampString.substr(12,2);
    var date = new Date(Date.UTC(year, month - 1, day, hour, minute, second, 0));
    return date;
}

function timeElapsedString(date){
    var responseString = "";
    var todayDate = new Date();
    var dateDiff = todayDate - date;
    var dayDiff = Math.floor((dateDiff)/(1000*60*60*24));
    var hourDiff = Math.floor((dateDiff)/(1000*60*60));
    var minDiff = Math.floor((dateDiff)/(1000*60));
    if(dayDiff >= 1 ){
        responseString = dayDiff + " day" + pluralize(dayDiff);
    } else if (hourDiff >= 1) {
        responseString = hourDiff + " hour" + pluralize(hourDiff);
    } else {
        responseString = minDiff + " minute" + pluralize(minDiff);
    }
    responseString = responseString + " ago";
    return responseString;
}

function pluralize(integer){
    if(integer >= 2){
        return "s";
    } else {
        return "";
    }
}