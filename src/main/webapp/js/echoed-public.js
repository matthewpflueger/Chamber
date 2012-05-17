var EventAggregator = _.extend({}, Backbone.Events);

Echoed = {
    Views: {
        Pages:{},
        Components:{}
    },
    Models:{},
    Collections:{},
    init: function() {
        Backbone.history.start();
    }
};

Echoed.Models.Product = Backbone.Model.extend({
    initialize: function(){
    }
});

Echoed.Views.Pages.Exhibit = Backbone.View.extend({
    el: '#content',
    initialize: function(options){
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.template = _.template($('#templates-pages-exhibit').html());
        this.componentTemplate = _.template($('#templates-components-product').html());

        if(!options.Filter)
            this.filter = '*';
        else
            this.filter = "." + options.Filter;

        this.jsonUrl = Echoed.urls.api + "/public/feed";
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
                self.element.html(self.template);
                self.exhibit = $('#exhibit');

                if(data.echoes.length > 0){
                    self.exhibit.isotope({
                        masonry:{
                            columnWidth: 5
                        },
                        itemSelector: '.item_wrap',
                        filter: self.filter
                    });
                    $.each(data.echoes, function(index,product){
                        var productModel = new Echoed.Models.Product(product);
                        self.addProduct(productModel, self.filter);
                    });
                }

                window.setTimeout(function(){
                    self.exhibit.isotope('destroy');
                    self.exhibit.remove();
                    self.exhibit = null;
                    self.element.empty();
                    self.render();
                }, 30000);
            }
        });

    },
    addProduct: function(productModel, filter) {
        var self = this;
        var productDiv = $('<div></div>');
        new Echoed.Views.Components.Product({
                el: productDiv,
                model: productModel,
                EvAg: self.EvAg,
                template: self.componentTemplate});
        var imageHeight = productModel.get("image").preferredHeight;
        if(imageHeight) {
            self.exhibit.isotope('insert', productDiv);
        } else {
            productDiv.imagesLoaded(function(){
                self.exhibit.isotope('insert', productDiv);
            });
        }
    }
});

Echoed.Views.Components.Product = Backbone.View.extend({
    initialize: function(options){
        this.el = options.el;
        this.EvAg = options.EvAg;
        this.state = 0;
        this.template = options.template; //_.template($('#templates-components-product').html());
        this.render();
    },
    render: function(){
        var self = this;
        var imageUrl = self.model.get("image").preferredUrl;
        var imageWidth = self.model.get("image").preferredWidth;
        var imageHeight = self.model.get("image").preferredHeight;
        self.el.addClass("item_wrap").css("cursor", "default").html(self.template);
        var img = self.el.find("img");
        var text = self.el.find(".item_text");
        text.append('<strong>' + self.model.get("partnerName") + '</strong><br/>');
        text.append(self.model.get("echoProductName"));
        img.attr('src', imageUrl);
        if (imageWidth > 0) {
            img.attr('width', imageWidth)
        }
        if (imageHeight > 0) {
            img.attr('height', imageHeight)
        }
        return self;
    }
});