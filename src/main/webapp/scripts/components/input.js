    define(
    [
        'jquery',
        'backbone',
        'underscore',
        'expanding',
        'components/utils',
        'components/select',
        'models/story',
        'hgn!templates/input/storySummary',
        'hgn!templates/input/storyCoverInput',
        'hgn!templates/input/storyCover',
        'hgn!templates/input/storyChapter',
        'hgn!templates/input/storyChapterInput',
        'cloudinary'
    ],
    function($, Backbone, _, expanding, utils, Select, ModelStory, templateSummary, templateStoryCoverInput, templateStoryCover, templateChapter, templateChapterInput){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.element = $(options.el);
                this.properties = options.properties;
                this.modelUser = options.modelUser;
                this.EvAg = options.EvAg;
                this.EvAg.bind("field/show", this.load);
                this.modelUser.on("change:id", this.login);
                this.locked = false;
                this.cloudName = "";
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
                'click .fade': "fadeClick",
                'click .text': "textClick",
                "click .photos": "photosClick",
                "click .link": "linkClick"
            },
            fadeClick: function(ev){
                if($(ev.target).hasClass("fade")){
                    this.close();
                }
            },
            linkClick: function(ev){
                $("#input-link").slideToggle('slow');
                $(ev.currentTarget).toggleClass("on");
            },
            photosClick: function(ev){
                $("#input-photos").slideToggle('slow');
                $(ev.currentTarget).toggleClass("on");
            },
            textClick: function(ev){
                $("#input-text").slideToggle('slow');
                $(ev.currentTarget).toggleClass("on");
            },
            login: function(){
                if(this.loaded === true) this.load(this.id, this.type);
            },
            load: function(id, type){
                var self = this;
                this.id = id;
                this.type = type;
                var loadData = {};
                loadData[type + "Id"] = id;

                if(this.modelUser.isLoggedIn()){
                    this.modelStory = new ModelStory(null, {
                        loadData: loadData,
                        properties: this.properties,
                        success: function(model){
                            self.render();
                        }
                    });
                } else if(type === "partner"){
                    var text = this.properties.partner.name + " wants to hear your story. Share your story and have it featured.";
                    this.EvAg.trigger("login/init", null, text, self.close);
                } else {
                    this.EvAg.trigger("login/init", null, text, self.close);
                }
            },
            unload: function(callback){
                var self = this;
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
                self.template = templateSummary();
                self.element.removeClass("small");
                self.element.html(self.template);

                self.cover = $('#field-summary-cover');
                self.body = $('#story-summary-body');

                if(this.modelStory.get("isNew")){
                    self.loadStoryInputTemplate({ type: "Add" });
                } else if(this.modelStory.get("chapters").length > 0){
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
                var story = this.modelStory.get("story");
                var template = templateStoryCover(this.modelStory.get("story"));
                self.cover.html(template);
                if (story.image !== null) {
                    utils.scaleByHeight(story.image, 50)
                            .addClass("story-summary-photo")
                            .appendTo(self.cover.find('.story-input-photo'));
                } else self.cover.find('.story-input-photo-row').hide();
            },
            loadChapterInputTemplate: function(option){
                var self = this;

                var chapter = this.modelStory.getChapter(option.index);
                var chapterImages = this.modelStory.getChapterImages(chapter.id);
                this.currentImages = [];
                this.editChapterId = chapter.id;

                var cElement = function(opt){
                    if(opt.type ==="Edit") return $("#chapter-row-" + opt.index);
                    else return $('<div class="field-main-row clearfix"></div>').appendTo(self.body);
                }(option);

                cElement.fadeOut(function(){
                    var template = templateChapterInput({ chapter: chapter });
                    $(this).html(template);
                    var chapterPhotos = $('#story-input-thumbnails');
                    var placeholder= $('#thumbnail-placeholder');
                    if(chapterImages.length) $('#input-photos').show();

                    $.each(chapterImages, function(index, chapterImage){
                        var thumbDiv = $('<div></div>').addClass("thumb").addClass('chapter-thumb').attr("index", index).attr("imageId",chapterImage.image.id);
                        var thumbX = $('<div></div>').addClass('chapter-thumb-x');
                        thumbDiv.append(thumbX);
                        var photo = utils.scaleByHeight(chapterImage.image, 75);
                        placeholder.before(thumbDiv.append(photo));
                        self.currentImages.push(chapterImage.image);
                    });

                    $("#chapter-text").expandingTextarea();

                    $('#photo-upload-button').cloudinary_fileupload({
                        dragover: function(e){
                            var dropZone = $('#thumb-placeholder'),
                                timeout = window.dropZoneTimeout;
                            if (!timeout) {
                                dropZone.addClass('in');
                            } else {
                                clearTimeout(timeout);
                            }
                            if (e.target === dropZone[0]) {
                                dropZone.addClass('hover');
                            } else {
                                dropZone.removeClass('hover');
                            }
                            window.dropZoneTimeout = setTimeout(function () {
                                window.dropZoneTimeout = null;
                                dropZone.removeClass('in hover');
                            }, 100);
                        },
                        progress: function(e,data){
                            var pct = data.loaded / data.total * 100;
                            $('#photo-upload-progress-fill').css({
                                width: pct + "%"
                            });
                        },
                        submit: function(e, data) {
                            $('#photo-upload-progress').show();

                            var storyId = self.modelStory.id;
                            var url = "/story/" + storyId + "/image";
                            var e = $(this);
                            $.ajax({
                                url: url,
                                type: "POST",
                                dataType: "json",
                                success: function(result) {
                                    e.fileupload('option', 'url', result.uploadUrl);
                                    data.formData = result;
                                    self.cloudName = result.cloudName;
                                    e.fileupload('send', data);
                                }
                            });
                            return false;
                        },
                        done: function(e, data) {
                            if (data.result.error) return;

                            var imageUrl = utils.imageUrl(data.result.public_id, self.cloudName);
                            var width = parseInt(data.result.width);
                            var height = parseInt(data.result.height);
                            var image = {
                                id : data.result.public_id,
                                url : imageUrl,
                                width : width,
                                height : height,
                                originalWidth : width,
                                originalHeight : height,
                                originalUrl : imageUrl,
                                preferredWidth : width,
                                preferredHeight : height,
                                preferredUrl : imageUrl,
                                storyUrl: imageUrl,
                                cloudName: self.cloudName,
                                isCloudinary: true
                            };

                            placeholder.before(
                            $('<div></div>')
                                    .addClass("thumb")
                                    .append(utils.scaleByHeight(image, 75))
                                    .hide()
                                    .appendTo(chapterPhotos)
                                    .fadeIn(function(){
                                    $('#photo-upload-progress').hide();
                                }));

                            self.currentImages.push(image);
                        },
                        failed: function(e, data) {
                            $('#photo-upload-progress-fill').addClass('failed');
                            $('#pohto-upload-progress-text').text('Failed')
                        }});

                    $('#thumb-placeholder').attr("src", self.properties.urls.images + "/bk_img_upload_ph.png");
                    $(this).addClass('highlight');
                    if(chapter.publishedOn > 0) $('#chapter-save').hide();

                    $(this).fadeIn();
                });
            },
            loadChapterTemplates: function(){
                var self = this;
                $.each(self.modelStory.get("chapters"), function(index, chapter){
                    chapter.index = index;
                    var template = templateChapter(chapter);
                    var chapterRow = $('<div class="field-main-row clearfix"></div>')
                            .html(template)
                            .appendTo(self.body)
                            .attr('id','chapter-row-' + index);
                    var photos = chapterRow.find('.story-input-photos');
                    var imagesFound = false;
                    $.each(self.modelStory.get("chapterImages"), function(index, chapterImage){
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
                var template = templateStoryCoverInput();
                if(this.modelStory.get("topic")) $('#field-title').text(this.modelStory.get("topic"));

                self.cover.fadeOut(function(){
                    $(this).html(template);

                    var partner = self.modelStory.get("partner");
                    var story = self.modelStory.get("story");

                    $('#story-name').val(self.modelStory.get("story").title);
                    $('#story-input-from-content').text(partner.name);
                    $('#story-input-partnerId').val(partner.id);
                    $('#story-input-from').show();


                    if(story.image !== null){
                        var photo = utils.scaleByWidth(story.image, 75);
                        $('#story-input-photo').attr({
                            src: photo.attr('src'),
                            width: photo.attr('width'),
                            height: photo.attr('height')});
                        $('#story-input-imageId').val(story.image.id);
                    }

                    $('#photo-upload-button').cloudinary_fileupload({
                        //dropZone: $('#photo-drop'),
                        dragover: function(e){
                            var dropZone = $('#photo-upload, #photo-preview'),
                                timeout = window.dropZoneTimeout;
                            if (!timeout) {
                                dropZone.addClass('in');
                            } else {
                                clearTimeout(timeout);
                            }
                            if (e.target === dropZone[0]) {
                                dropZone.addClass('hover');
                            } else {
                                dropZone.removeClass('hover');
                            }
                            window.dropZoneTimeout = setTimeout(function () {
                                window.dropZoneTimeout = null;
                                dropZone.removeClass('in hover');
                            }, 100);
                        },
                        progress: function(e,data){
                            var pct = data.loaded / data.total * 100;
                            $('#photo-upload-progress-fill').css({
                                width: pct + "%"
                            });
                        },
                        submit: function(e, data) {
                            $('#photo-upload-progress').show();
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
                                    self.cloudName = result.cloudName;
                                    e.fileupload('send', data);
                                }
                            });
                            return false;
                        },
                        done: function(e, data) {
                            if (data.result.error) return;

                            var imageUrl = utils.imageUrl(data.result.public_id, self.cloudName);
                            var width = parseInt(data.result.width);
                            var height = parseInt(data.result.height);
                            var image = {
                                id : data.result.public_id,
                                url : imageUrl,
                                width : width,
                                height : height,
                                originalWidth : width,
                                originalHeight : height,
                                originalUrl : imageUrl,
                                preferredWidth : width,
                                preferredHeight : height,
                                preferredUrl : imageUrl,
                                storyUrl: imageUrl,
                                cloudName: self.cloudName,
                                isCloudinary: true
                            };
                            var photo = utils.fit(image, 120, 120);
                            $("#story-input-photo").fadeOut().attr("src", photo.attr("src")).fadeIn(function(){
                                $('#photo-upload-progress').hide();
                            });
                            $('#story-input-imageId').val(JSON.stringify(image));
                            self.data.imageId = image.id;
                        },
                        failed: function(e, data) {
                            $('#photo-upload-progress-fill').addClass('failed');
                            $('#photo-upload-progress-text').text('Failed')
                        }});

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
                    var imageId = $('#story-input-imageId').val() ?  $('#story-input-imageId').val() : null;
                    var partnerId = $('#story-input-partnerId').val() ? $('#story-input-partnerId').val() : null;
                    var storyData = {
                        storyId: this.modelStory.id,
                        title: title,
                        imageId: imageId,
                        partnerId: partnerId
                    };
                    this.modelStory.submitCover(storyData, function(model){
                        self.locked = false;
                        self.render();
                    });
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
                    window.location.hash = "#!story/" + this.modelStory.id
                });
            },
            cancelChapterClick: function(){
                var self = this;
                if(self.data.storyFull.chapters.length > 0) self.load(self.data.storyFull.story.id, "story");
                else self.close();
            },
            hideStoryClick: function() {
                var v = confirm("Are you sure you want to hide this story?");
                if(v === true) this.modelStory.moderate();
            },
            updateChapter: function(publishOption){
                var self = this;
                var title = $.trim($('#chapter-title').val());
                var text = $.trim($('#chapter-text').val());
                var images = self.currentImages ? self.currentImages : [];
                images = _.map(images, function(img) { return JSON.stringify(img); });
                if(text === "" && images.length <= 0){
                    alert("You must have either a description or an image");
                } else if(self.locked === false){
                    self.locked = true;
                    var options = {
                        title: title,
                        text: text,
                        imageIds: images,
                        publish: publishOption
                    };
                    if(this.editChapterId) options.chapterId = this.editChapterId;
                    this.modelStory.saveChapter(options, function(model, response){
                        self.locked = false;
                        self.render();
                    });
               }
            },
            hideSubmits: function(){
                this.element.find('.field-edit').hide();
                this.element.find('.story-summary-buttons').hide();
            },
            show: function(){
                var self = this;
                self.element.fadeIn();
                $("body").addClass("noScroll");
                $("#story-name").focus();
            },
            close: function(){
                var self = this;
                this.loaded = false;
                self.element.fadeOut().empty();
                $("body").removeClass("noScroll");
                self.EvAg.trigger('hash/reset');
            }
        });

    }
)