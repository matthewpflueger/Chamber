define(
    [
        'jquery',
        'backbone',
        'underscore',
        'hgn!views/forum/forum',
        'components/utils'
    ],
    function($, Backbone, _, templateForum, utils){
        return Backbone.View.extend({
            initialize: function(options){
                _.bindAll(this);
                this.el =           options.el;
                this.element =      $(this.el);
                this.properties =   options.properties;
                this.modelUser =    options.modelUser;
                this.modelStory =   options.modelStory;
                this.EvAg =         options.EvAg;
                this.personal =     options.Personal;
                this.render();
            },
            events: {
                "click": "click"
            },
            render: function(){

                var chapterText =   "";
                if(this.modelStory.get("chapters").length){
                    chapterText =   this.modelStory.get("chapters")[0].text;
                    var len =       1000;
                    if(chapterText.length > len){
                        c =         chapterText.substr(len).split(/[.!?]/)[0];
                        c =         chapterText.substr(0, len) + c + chapterText.substr(len +c.length, 1);
                        chapterText = c;
                    }
                }

                var profilePhotoUrl =   utils.getProfilePhotoUrl(this.modelStory.get("echoedUser"), this.properties.urls);
                var view = {
                    profilePhotoUrl:    profilePhotoUrl,
                    storyFull:          this.modelStory.toJSON(),
                    chapterText:        chapterText
                };
                var template = templateForum(view);

                this.element.addClass('itemForum');
                this.element.html(template);
            },
            click: function(ev){
                var target =    $(ev.target);
                if(!target.is('a')){
                    if(this.modelStory.get("chapters").length > 0){
                        window.location.hash = "#!story/" +  this.modelStory.id;
                    } else {
                        window.location.hash = "#!write/" +  this.modelStory.id;
                    }
                }
            }

        });
    }
)