define(
    'components/product',
    ['jquery', 'backbone', 'underscore', 'components/utils'],
    function($, Backbone, _, templateProduct, utils){
        return Backbone.View.extend({
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
                var template = _.template(templateProduct);
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
                text.append(utils.timeElapsedString(boughtOnDate) + "<br/>");

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
                window.location.hash = "#!partner/" + id;
            },
            click: function(e){
                if(this.personal){
                    window.location.hash = "#!write/echo/" + this.el.attr("id");
                } else {
                    var href = this.el.attr("href");
                    window.open(href);
                }
            }
        });
    }
)
