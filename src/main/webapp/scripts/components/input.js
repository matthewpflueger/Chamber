    define(
    [
        'jquery',
        'backbone',
        'underscore',
        'expanding',
        'components/utils',
        'components/select',
        'components/ajaxInput',
        'text!templates/input/storySummary.html',
        'text!templates/input/storyCoverInput.html',
        'text!templates/input/storyCover.html',
        'text!templates/input/storyChapter.html',
        'text!templates/input/storyChapterInput.html',
        'text!templates/input/storyLogin.html',
        'cloudinary'
    ],
    function($, Backbone, _, expanding, utils, Select, AjaxInput, templateSummary, templateStoryCoverInput, templateStoryCover, templateChapter, templateChapterInput, templateStoryLogin){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element = $(options.el);
                this.properties = options.properties;
                this.EvAg = options.EvAg;
                this.EvAg.bind("field/show", this.load);
                this.EvAg.bind("fade/click", this.close);
                if(this.properties.isWidget === true){
                    this.EvAg.bind("user/login", this.login);
                }
                this.locked = false;
                this.prompts = [];
            },
            events: {
                "click .field-close" : "close",
                "click #edit-cover": "editStoryClick",
                "click .edit-chapter" : "editChapterClick",
                'click #submit-cover': "submitCover",
                'click #chapter-publish': "publishChapterClick",
                'click #chapter-save': 'saveChapterClick',
                'click #chapter-add': 'addChapterClick',
                'click #story-finish': 'finishStoryClick',
                'click #story-hide': "hideStoryClick",
                'click #chapter-cancel': 'cancelChapterClick',
                'click .chapter-thumb-x': 'removeChapterThumb',
                "click .input-login-button": "loginClick"
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
                loadData[type + "Id"] = id;
                self[type+"Id"] = id;
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
            loginClick: function(ev){
                var self = this;
                if(self.properties.isWidget){
                    var target = $(ev.currentTarget);
                    var href = target.attr('href');
                    window.open(href, "Echoed",'width=800,height=440,toolbar=0,menubar=0,location=0,status=1,scrollbars=0,resizable=0,left=0,top=0');
                    return false;
                }
            },
            renderLogin: function(data){
                var self = this;
                self.template = _.template(templateStoryLogin);
                self.element.html(self.template).addClass('small');
                $('#field-logo-img').attr("src", self.properties.urls.images + "/logo_large.png");

                if(self.properties.isWidget){
                    $("#field-fb-login").attr("href", utils.getFacebookLoginUrl("redirect/close"));
                    $("#field-tw-login").attr("href", utils.getTwitterLoginUrl("redirect/close"));
                    $('#field-user-login').attr('href', self.properties.urls.api + "/" + utils.getLoginRedirectUrl("redirect/close"));
                    $('#field-user-signup').attr("href", self.properties.urls.api + "/" + utils.getSignUpRedirectUrl("redirect/close"));

                } else {
                    $('#field-user-login').attr('href', self.properties.urls.api + "/" + utils.getLoginRedirectUrl());
                    $('#field-user-signup').attr("href", self.properties.urls.api + "/" + utils.getSignUpRedirectUrl());
                    $("#field-fb-login").attr("href", utils.getFacebookLoginUrl(window.location.hash));
                    $("#field-tw-login").attr("href", utils.getTwitterLoginUrl(window.location.hash));
                }
                var body = self.element.find(".field-login-body");

                if(data){
                    var bodyText = data.partner.name + " wants to hear your story. Share your story and have it featured on the " + data.partner.name + " page.";
                    var bodyTextNode = $('<div class="field-login-body-text"></div>').text(bodyText);
                    body.append(bodyTextNode);
                }

                self.show();
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
            render: function(){
                var self = this;
                self.element.empty();
                self.locked = false;
                self.template = _.template(templateSummary);
                self.element.removeClass("small");
                self.element.html(self.template);

                self.cover = $('#field-summary-cover');
                self.body = $('#story-summary-body');

                if(self.data.storyFull.isNew){
                    self.loadStoryInputTemplate({ type: "Add" });
                } else if(self.data.storyFull.chapters.length > 0){
                    self.loadStoryCoverTemplate();
                    self.loadChapterTemplates();
                } else {
                    self.loadStoryCoverTemplate();
                    self.loadChapterInputTemplate({ type: "Add" });
                }
                self.show();
            },
            removeChapterThumb: function(e){
                var self = this;
                var target = $(e.currentTarget).parent();
                var id = target.attr("imageId");
                var i = 0;
                for(i = 0; i < self.currentChapter.images.length; i++){
                    if(self.currentChapter.images[i].id === id) self.currentChapter.images.splice(i, 1);
                }
                target.fadeOut(function(){$(this).remove()});
            },
            editStoryClick: function(ev){
                this.loadStoryInputTemplate({ type: "Edit" });
            },
            editChapterClick: function(ev){
                var chapterIndex = $(ev.currentTarget).attr('chapterIndex');
                var chapterId = $(ev.currentTarget).attr('chapterId');
                this.loadChapterInputTemplate({ type: "Edit", index: chapterIndex, chapterId: chapterId })
            },
            loadStoryCoverTemplate: function(){
                var self = this;
                var story = self.data.storyFull.story;
                var template = _.template(templateStoryCover, story);
                self.cover.html(template);

                if (story.image !== null) {
                    utils.scaleByHeight(story.image, 50)
                            .addClass("story-summary-photo")
                            .appendTo(self.cover.find('.story-input-photo'));
                } else self.cover.find('.story-input-photo-row').hide();

                if(!story.community) $('#story-community').hide();

                if(story.productInfo !== null) $('#story-info').show();
                else $('#story-info').hide();

            },
            loadChapterInputTemplate: function(option){
                var self = this;
                var template = _.template(templateChapterInput);
                var chapter = function(opt){
                    if(opt.type ==="Edit") return $("#chapter-row-" + opt.index);
                    else return $('<div class="field-main-row clearfix"></div>').appendTo(self.body);
                }(option);
                self.currentChapter = {
                    images: []
                };
                chapter.fadeOut(function(){
                    $(this).html(template);

                    var selectOptions = {
                        optionsArray: [],
                        el: '#chapter-title',
                        freeForm: "(Write Your Own Topic)",
                        edit: true,
                        default: null
                    };

                    self.chapterPhotos = $('#story-input-thumbnails');

                    if(option.type==="Edit"){
                        self.currentChapter = self.data.storyFull.chapters[option.index];
                        self.currentChapter.images = [];
                        $('#chapter-text').val(self.currentChapter.text);
                        selectOptions.optionsArray.push(self.currentChapter.title);
                        $.each(self.data.storyFull.chapterImages, function(index, chapterImage){
                            if(chapterImage.chapterId === self.currentChapter.id){
                                var thumbDiv = $('<div></div>').addClass("thumb").addClass('chapter-thumb').attr("index", index).attr("imageId",chapterImage.image.id);
                                var thumbX = $('<div></div>').addClass('chapter-thumb-x');
                                thumbDiv.append(thumbX);
                                var photo = utils.scaleByHeight(chapterImage.image, 75);
                                thumbDiv.append(photo).appendTo(self.chapterPhotos).fadeIn();
                                self.currentChapter.images.push(chapterImage.image);
                            }
                        });
                    }

                    $("#chapter-text").expandingTextarea();

                    $('#photo-upload').cloudinary_fileupload({
                        submit: function(e, data) {
                            $('.qq-upload-list').addClass('qq-upload-spinner');

                            var storyId = self.data.storyFull.id;
                            var url = "/story/" + storyId + "/image";
                            var e = $(this);
                            $.ajax({
                                url: url,
                                type: "POST",
                                dataType: "json",
                                success: function(result) {
                                    e.fileupload('option', 'url', result.uploadUrl);
                                    data.formData = result;
                                    e.fileupload('send', data);
                                }
                            });
                            return false;
                        }
                    }).bind('fileuploaddone', function(e, data) {
                        if (data.result.error) return;
                        $('.qq-upload-list').removeClass('qq-upload-spinner').text("Success!");

                        var image = {
                            id : data.result.public_id,
                            url : data.result.url,
                            width : data.result.width,
                            height : data.result.height,
                            originalWidth : data.result.width,
                            originalHeight : data.result.height,
                            originalUrl : data.result.url,
                            preferredWidth : data.result.width,
                            preferredHeight : data.result.height,
                            preferredUrl : data.result.url,
                            cloud_name: data.formData.cloud_name
                        }

                        $('<div></div>')
                                .addClass("thumb")
                                .append(utils.scaleByHeight(image, 75))
                                .hide()
                                .appendTo(self.chapterPhotos)
                                .fadeIn();

                        self.currentChapter.images.push(image);
                    }).bind('fileuploadfailed', function(e, data) {
                        $('.qq-upload-list').removeClass('qq-upload-spinner').text("Failed :(");
                    });


                    $.each(self.data.storyPrompts.prompts, function(index, prompt){
                        var inChapters = false;
                        $.each(self.data.storyFull.chapters, function(index, chapter){
                            if(prompt === chapter.title) inChapters = true;
                        });
                        if(inChapters === false) selectOptions.optionsArray.push(prompt)
                    });
                    self.hideSubmits();
                    self.select = new Select(selectOptions);
                    $('#thumb-placeholder').attr("src", self.properties.urls.images + "/bk_img_upload_ph.png");
                    $(this).addClass('highlight');
                    if(self.currentChapter.publishedOn > 0) $('#chapter-save').hide();

                    $(this).fadeIn();
                });

            },
            loadChapterTemplates: function(){
                var self = this;
                $.each(self.data.storyFull.chapters, function(index, chapter){
                    chapter.index = index;
                    var template = _.template(templateChapter, chapter);
                    var chapterRow = $('<div class="field-main-row clearfix"></div>')
                            .html(template)
                            .appendTo(self.body)
                            .attr('id','chapter-row-' + index);
                    var photos = chapterRow.find('.story-input-photos');
                    var imagesFound = false;
                    $.each(self.data.storyFull.chapterImages, function(index, chapterImage){
                        if(chapterImage.chapterId === chapter.id){
                            var chapterImg = utils.scaleByHeight(chapterImage.image, 50).addClass('story-summary-photo');
                            photos.append(chapterImg);
                            imagesFound = true
                        }
                    });
                    if (imagesFound === false) chapterRow.find('.story-input-photo-row').hide();
                    if (chapter.publishedOn > 0) {
                        chapterRow.find('.story-input-publishedOn').text("Published");
                        $("#story-hide").hide();
                    } else chapterRow.find('.story-input-publishedOn').text("Draft").addClass('highlight-text').addClass("bold");

                });
            },
            loadStoryInputTemplate: function(option){
                var self = this;
                var template = _.template(templateStoryCoverInput);
                self.cover.fadeOut(function(){
                    $(this).html(template);
                    $('#story-input-photo').attr("src", self.properties.urls.images + "/bk_img_upload_ph.png");
                    $('#submit-type').val("POST");

                    var defaultCommunity = self.data.partner.category;
                    if(self.data.storyFull){
                        if(self.data.storyFull.story.community) defaultCommunity = self.data.storyFull.story.community;
                    }

                    var selectOptions = {
                        optionsArray: self.data.communities.communities,
                        el: '#story-input-community',
                        default: defaultCommunity,
                        freeForm: null,
                        edit: false
                    };

                    if(self.data.partner.name !== "Echoed") selectOptions.locked = true;
                    self.communitySelect = new Select(selectOptions);

                    if(option.type === "Edit"){
                        $('#story-name').val(self.data.storyFull.story.title);
                        $('#submit-type').val("PUT");

                        if(self.data.storyFull.story.image !== null){
                            var image = self.data.storyFull.story.image;
                            var photo = utils.scaleByWidth(image, 75);
                            $('#story-input-photo').attr({
                                    src: photo.attr('src'),
                                    width: photo.attr('width'),
                                    height: photo.attr('height')});
                            $('#story-input-imageId').val(self.data.storyFull.story.image.id);
                        }
                    }
                    if(self.data.partner.name !== "Echoed"){
                        $('#story-input-from-content').text(self.data.partner.name);
                        $('#story-input-partnerId').val(self.data.partner.id);
                        $('#story-input-from').show();
                    } else {
                        $('#story-input-from').hide();
                        if(self.data.storyFull !== null){
                            if(self.data.storyFull.story.productInfo !== null){
                                $('#story-input-partnerId').val(self.data.partner.id);
                                $('#story-input-from-content').text(self.data.storyFull.story.productInfo);
                                $('#story-input-from').show();
                            }
                        }
                    }

                    $('#field-photo-upload').cloudinary_fileupload({
                        submit: function(e, data) {
                            $('.qq-upload-list').addClass('qq-upload-spinner');

                            var storyId = self.data.storyFull.id;
                            var url = "/story/" + storyId + "/image";
                            var e = $(this);
                            $.ajax({
                                url: url,
                                type: "POST",
                                dataType: "json",
                                success: function(result) {
                                    e.fileupload('option', 'url', result.uploadUrl);
                                    data.formData = result;
                                    e.fileupload('send', data);
                                }
                            });
                            return false;
                        }
                    }).bind('fileuploaddone', function(e, data) {
                        if (data.result.error) return;
                        $('.qq-upload-list').removeClass('qq-upload-spinner').text("Success!");

                        var image = {
                            id : data.result.public_id,
                            url : data.result.url,
                            width : data.result.width,
                            height : data.result.height,
                            originalWidth : data.result.width,
                            originalHeight : data.result.height,
                            originalUrl : data.result.url,
                            preferredWidth : data.result.width,
                            preferredHeight : data.result.height,
                            preferredUrl : data.result.url,
                            cloud_name: data.formData.cloud_name
                        }
                        var photo = utils.fit(image, 120, 120);

                        $("#story-input-photo").fadeOut().attr("src", photo.attr("src")).fadeIn();
                        $('#story-input-imageId').val(JSON.stringify(image));
                        self.data.imageId = image.id;
                    }).bind('fileuploadfailed', function(e, data) {
                        $('.qq-upload-list').removeClass('qq-upload-spinner').text("Failed :(");
                    });

                    self.hideSubmits();
                    $(this).addClass("highlight").fadeIn();
                });
            },
            submitCover: function(){
                var self = this;
                var title = $('#story-name').val();
                if($.trim(title) === ""){
                    alert("Please include a title for your story");
                } else if(self.locked !== true){
                    var type = $('#submit-type').val();
                    var imageId = $('#story-input-imageId').val() ?  $('#story-input-imageId').val() : null;
                    var partnerId = $('#story-input-partnerId').val() ? $('#story-input-partnerId').val() : null;
                    var echoId = $('#story-input-echoId').val() ? $('#story-input-echoId').val() : null;
                    var productInfo = $.trim($('#story-input-from-content').html()) ? $.trim($('#story-input-from-content').html()) : null;

                    var community = self.communitySelect.val() ? self.communitySelect.val() : null;

                    storyData = {
                        storyId: self.data.storyFull.id,
                        title: title
                    };

                    if(echoId !== null && type === "POST") storyData.echoId = echoId;
                    if(partnerId !== null && type === "POST") storyData.partnerId = partnerId;
                    if(productInfo !== null) storyData.productInfo = productInfo;
                    if(imageId !== null) storyData.imageId = imageId;
                    if(community !== null) storyData.community = community;

                    var url = "";
                    if(type === "PUT") url = self.properties.urls.api +"/story/" + self.data.storyFull.story.id;
                    else url = self.properties.urls.api + "/story";
                    utils.AjaxFactory({
                        url: url,
                        type: type,
                        data: storyData,
                        success: function(resp){
                            self.locked = false;
                            self.load(resp.id, "story");
                        }
                    })();
                }
            },
            addChapterClick: function(){
                this.loadChapterInputTemplate({ type: "Add" });
            },
            publishChapterClick: function(){
                this.updateChapter(true);
            },
            saveChapterClick: function(){
                this.updateChapter(false);
            },
            finishStoryClick: function(){
                var self = this;
                self.unload(function(){
                    self.EvAg.trigger('router/me');
                    window.location.hash = "#!story/" + self.data.storyFull.story.id;
                });
            },
            cancelChapterClick: function(){
                var self = this;
                if(self.data.storyFull.chapters.length > 0) self.load(self.data.storyFull.story.id, "story");
                else self.close();
            },
            hideStoryClick: function() {
                var self = this;
                var id = self.data.storyFull.story.id
                var echoedUserId = self.properties.echoedUser.id
                utils.AjaxFactory({
                    url: this.properties.urls.api + "/story/" + id + "/moderate",
                    type: "POST",
                    data: {
                        moderated: true,
                        storyOwnerId : echoedUserId
                    },
                    success: function() {
                        self.unload(function() {
                            self.EvAg.trigger('router/me');
                        });
                    }
                })();
            },
            updateChapter: function(publishOption){
                var self = this;
                var title = $.trim(self.select.val());
                var text = $.trim($('#chapter-text').val());

                var images = self.currentChapter.images ? self.currentChapter.images : [];
                images = _.map(images, function(img) { return JSON.stringify(img); });

                if(title === ""){
                    alert("Please write or choose a topic");
                } else if(text === "" && images.length <= 0){
                    alert("You must have either a description or an image");
                } else if(self.locked === false){
                    self.locked = true;
                    if(self.currentChapter.id !== undefined){
                        utils.AjaxFactory({
                            url: self.properties.urls.api + "/story/" + self.data.storyFull.story.id + "/chapter/" + self.currentChapter.id,
                            type: "PUT",
                            processData: false,
                            contentType: "application/json",
                            data: JSON.stringify({
                                title: title,
                                text: text,
                                imageIds: images,
                                publish: publishOption
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
                                title: title,
                                text: text,
                                imageIds: images,
                                publish: publishOption
                            }),
                            success: function(chapterSubmitResponse) {
                                self.locked = false;
                                self.load(self.data.storyFull.story.id, "story");
                            }
                        })();
                    }
               }
            },
            hideSubmits: function(){
                this.element.find('.field-edit').hide();
                this.element.find('.story-summary-buttons').hide();
            },
            show: function(){
                var self = this;
                self.EvAg.trigger('fade/show');
                self.element.fadeIn();
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