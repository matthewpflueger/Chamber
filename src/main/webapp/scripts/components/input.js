define(
    [
        'jquery',
        'backbone',
        'underscore',
        'fileuploader',
        'expanding',
        'components/utils',
        'components/select',
        'components/ajaxInput',
        'text!templates/story-edit.html',
        'text!templates/story-input.html',
        'text!templates/story-summary.html',
        'text!templates/story-login.html',
        'text!templates/input/storySummaryRow.html'
    ],
    function($, Backbone, _, qq, expanding, utils, Select, AjaxInput, templateStoryEdit, templateStoryInput, templateStorySummary, templateStoryLogin, templateStorySummaryRow){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this, 'render', 'unload', 'login', 'load', 'loadStoryTemplate', 'submitInitStory', 'loadChapterTemplate', 'cancelChapter','submitChapter', 'storyEditClick', 'removeChapterThumb', 'addCategory');
                this.element = $(options.el);
                this.properties = options.properties;
                this.EvAg = options.EvAg;
                this.EvAg.bind("field/show", this.load);
                if(this.properties.isWidget === true){
                    this.EvAg.bind("user/login", this.login);
                }
                this.locked = false;
                this.prompts = [];
            },
            events: {
                "click .field-close" : "close",
                "click .field-submit" : "submitInitStory",
                "click .chapter-submit": "submitChapter",
                "click .chapter-publish": "publishChapter",
                "click .chapter-cancel": "cancelChapter",
                "click .chapter-thumb-x": "removeChapterThumb",
                "click .story-summary-submit": "storySummarySubmit",
                "click .story-summary-chapter-edit": "storyEditClick",
                "click #story-preview-edit": "loadStoryTemplate",
                "click #category-add": "addCategory",
                "click #field-main-category-edit": "editCategory"

            },
            login: function(echoedUser){
                this.properties.echoedUser = echoedUser;
                this.loadPartner();
            },
            loadPartner: function(){
                var self = this;
                if(self.loaded === true){
                    self.element.fadeOut(function(){
                        self.load(self.properties.partnerId, "partner");
                    });
                }
            },
            load: function(id, type){
                var self = this;
                var jsonUrl =  self.properties.urls.api + "/story";
                var loadData = {};
                self.loaded = true;
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
                if(self.properties.echoedUser){
                    utils.AjaxFactory({
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
                        utils.AjaxFactory({
                            url: self.properties.urls.api + "/api/partner/" + id,
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
            addCategory: function(){
                var self = this;
                var category = $('#ajax-input').find(".input-field").val();
                category = $.trim(category.replace(/[^a-zA-Z 0-9-_]+/g,''));
                var cArray = category.split(" ");
                $.each(cArray, function(index, string){
                    string = string.toLowerCase();
                    cArray[index] = string.substring(0,1).toUpperCase() + string.slice(1);
                });
                category = cArray.join(" ");

                utils.AjaxFactory({
                    url: self.properties.urls.api + "/story/" + self.data.storyFull.id + "/tag",
                    type: "POST",
                    dataType: 'json',
                    data: {
                        tagId: category
                    },
                    success: function(data){
                        self.data.storyFull.story = data;
                        self.loadStorySummary();
                        self.EvAg.trigger('category/refresh');
                    }
                })();
            },
            editCategory: function(){
                $('#ajax-input').fadeIn();
            },
            submitInitStory: function(){
                var self = this;
                var type = $('#submit-type').val();
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
                        if(type==="PUT"){
                            console.log(title);
                            utils.AjaxFactory({
                                url: self.properties.urls.api + "/story/" + self.data.storyFull.story.id,
                                type: 'PUT',
                                data: {
                                    title: title,
                                    imageId: self.data.imageId,
                                    productInfo: productFrom
                                },
                                success: function(createStoryResponse){
                                    self.load(createStoryResponse.id, "story");
                                    self.locked = false;
                                }
                            })();
                        } else {
                            utils.AjaxFactory({
                                url: self.properties.urls.api + "/story",
                                type: 'POST',
                                data: storyData,
                                success: function(createStoryResponse){
                                    self.load(createStoryResponse.id, "story");
                                    self.locked = false;
                                }
                            })();
                        }
                    }
                }
            },
            loadStorySummary: function(){
                var self = this;
                var template = _.template(templateStorySummary);
                self.element.html(template);
                self.element.find(".story-preview-title").text(self.data.storyFull.story.title);
                self.element.find(".story-preview-from").text(self.data.storyFull.story.productInfo);
                self.element.find(".story-preview-by").text(self.data.storyFull.echoedUser.name);
                $("#story-preview-photo").attr("src", self.data.storyFull.story.image.preferredUrl);

                $("#field-main-category-content").text(self.data.storyFull.story.tag);
                this.ajaxInput = new AjaxInput({ el: '#ajax-input', EvAg: self.EvAg, properties: this.properties });
                self.element.chapters = self.element.find('.story-summary-body');
                $.each(self.data.storyFull.chapters, function(index, chapter){

                    var chapterDiv = $('<div class="story-summary-chapter clearfix"></div>');
                    var t = _.template(templateStorySummaryRow, chapter);
                    chapterDiv.html(t);
                    chapterDiv.find('.story-summary-chapter-edit').attr('chapterId', index);

                    var chapterPhotos = $('<div class="story-summary-chapter-photo-container"></div>');
                    var chapterPhotosRow = $("<div class='story-summary-chapter-row'></div>").append($('<label>Photos: </label>')).append(chapterPhotos);
                    $.each(self.data.storyFull.chapterImages, function(index, chapterImage){
                        if(chapterImage.chapterId === chapter.id){
                            var chapterImg = $('<img class="story-summary-chapter-photo"/>').attr("height", 50).attr("src",chapterImage.image.preferredUrl);
                            chapterPhotos.append(chapterImg);
                        }
                    });
                    chapterDiv.append(chapterPhotosRow);
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
                            self.EvAg.trigger('router/me');
                            window.location.hash = "#!story/" + self.data.storyFull.story.id;
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
                self.template = _.template(templateStoryEdit);
                self.element.html(self.template);


                self.element.find(".story-preview-title").text(self.data.storyFull.story.title);
                self.element.find(".story-preview-from").text(self.data.storyFull.story.productInfo);
                self.element.find(".story-preview-by").text(self.data.storyFull.echoedUser.name);
                $("#thumb-placeholder").attr("src", self.properties.urls.images + "/bk_img_upload_ph.png" );
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
                            var thumbX = $('<div></div>').addClass('chapter-thumb-x');
                            thumbDiv.append(thumbX);
                            var photo = $('<img />').attr('src', chapterImage.image.preferredUrl).css(utils.getImageSizing(chapterImage, 75));
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

                self.select = new Select(selectOptions);
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
            publishChapter: function(e){
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

                            utils.AjaxFactory({
                                url: self.properties.urls.api + "/story/" + self.data.storyFull.story.id + "/chapter/" + self.currentChapter.id,
                                type: "PUT",
                                processData: false,
                                contentType: "application/json",
                                data: JSON.stringify({
                                    title: self.currentChapter.title,
                                    text: self.currentChapter.text,
                                    imageIds: self.currentChapter.images,
                                    publish: true
                                }),
                                success: function(chapterSubmitResponse) {
                                    self.locked = false;
                                    self.load(self.data.storyFull.story.id, "story");
                                }
                            })();
                        } else {
                            utils.AjaxFactory({
                                url: self.properties.urls.api + "/story/" + self.data.storyFull.story.id + "/chapter",
                                type: "POST",
                                processData: false,
                                contentType: "application/json",
                                data: JSON.stringify({
                                    title: self.currentChapter.title,
                                    text: self.currentChapter.text,
                                    imageIds: self.currentChapter.images,
                                    publish: true
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

                            utils.AjaxFactory({
                                url: self.properties.urls.api + "/story/" + self.data.storyFull.story.id + "/chapter/" + self.currentChapter.id,
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
                            utils.AjaxFactory({
                                url: self.properties.urls.api + "/story/" + self.data.storyFull.story.id + "/chapter",
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
                self.template = _.template(templateStoryLogin);
                self.element.html(self.template).addClass('small');
                $('#field-logo-img').attr("src", self.properties.urls.images + "/logo_large.png");
                if(self.properties.isWidget){
                    $("#field-fb-login").attr("href", utils.getFacebookLoginUrl("redirect/close")).attr("target","_blank");
                    $("#field-tw-login").attr("href", utils.getTwitterLoginUrl("redirect/close")).attr("target","_blank");
                } else {
                    $("#field-fb-login").attr("href", utils.getFacebookLoginUrl(window.location.hash));
                    $("#field-tw-login").attr("href", utils.getTwitterLoginUrl(window.location.hash));
                }
                $('#field-user-login').attr('href', utils.getLoginRedirectUrl());
                $('#field-user-signup').attr("href", utils.getSignUpRedirectUrl());
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
                self.template = _.template(templateStoryInput);
                self.element.html(self.template).removeClass("small");
                var type = "";

                self.data.imageId = null;

                if(self.data.storyFull !== null){
                    $('#story-name').val(self.data.storyFull.story.title);
                    $('#story-from').val(self.data.storyFull.story.productInfo);
                    $('#field-photo, #story-preview-photo').attr("src", self.data.storyFull.story.image.preferredUrl);
                    self.data.imageId = self.data.storyFull.story.imageId;
                    $('#submit-type').val('PUT');

                } else{
                    $('#field-photo').attr("src", self.properties.urls.images + '/bk_img_upload_ph.png');
                    $('#story-preview-photo').attr('src', self.properties.urls.images + '/bk_cover_default.jpg');
                    $('#submit-type').val('POST');
                }


                if (self.data.partner && self.data.partner.name != "Echoed"){
                    $("#story-from").val(self.data.partner.name).attr("readonly",true);
                    self.element.find('.field-title').text("Share Your " + self.data.partner.name + " Story");
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

    }
)