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
        _.bindAll(this,'render','addProduct');
        this.EvAg = options.EvAg;
        this.EvAg.bind('products/add', this.addProduct);
        this.EvAg.bind('filter/change', this.filterProducts);
        this.EvAg.bind('exhibit/relayout', this.relayout);
        this.productCount = 0;
        this.productCount2 = 0;
        this.element = $(this.el);
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
                var template = _.template($('#templates-pages-exhibit').html());
                self.element.html(template);
                self.exhibit=$('#exhibit');

                var exhibit = $('#exhibit');
                if(data.echoes.length > 0){
                    exhibit.isotope({
                        masonry:{
                            columnWidth: 5
                        },
                        itemSelector: '.item_wrap',
                        filter: self.filter
                    });
                    $.each(data.echoes, function(index,product){
                        var productModel = new Echoed.Models.Product(product);
                        self.addProduct(productModel,self.filter);
                    });
                }
                window.setTimeout(function(){
                    self.element.empty();
                    self.render();
                }, 30000);
            }
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

Echoed.Views.Components.Product = Backbone.View.extend({
    initialize: function(options){
        this.el = options.el;
        this.EvAg = options.EvAg;
        this.state =0;
        this.render();
    },
    render: function(){
        var template = _.template($('#templates-components-product').html());
        var self = this;
        var imageUrl =   this.model.get("image").preferredUrl;
        var imageWidth = this.model.get("image").preferredWidth;
        var imageHeight = this.model.get("image").preferredHeight;
        this.el.addClass("item_wrap").css("cursor", "default").html(template);
        var img = this.el.find("img");
        var text = this.el.find(".item_text");
        text.append('<strong>' + this.model.get("partnerName") + '</strong><br/>');
        text.append(this.model.get("echoProductName"));
        img.attr('src', imageUrl);
        if (imageWidth > 0) {
            img.attr('width', imageWidth)
        }
        if (imageHeight > 0) {
            img.attr('height', imageHeight)
        }
        return this;
    }
});