if (!('Echoed' in this)) this.Echoed = {};

$(document).ready(function() {
    Echoed.init();
});

var EventAggregator = _.extend({}, Backbone.Events);

Echoed = {
    Views: {
        Pages:{},
        Components:{}
    },
    Models:{},
    init: function() {
        var pageModel = new Echoed.Models.Page();
        var router = new Echoed.Router({EvAg: EventAggregator});
        var subNav = new Echoed.Views.Components.SubNav({EvAg: EventAggregator});
        var nav = new Echoed.Views.Components.Nav({EvAg: EventAggregator});
        var title = new Echoed.Views.Components.Title({EvAg: EventAggregator});
        router.bind('all', function(route) {
        });

        Backbone.history.start();
    }
};

Echoed.Models.Page = Backbone.Model.extend({
    defaults:{
        title: 'Summary',
        currentNav: 'summary_nav'
    },
    initialize: function(){
    }
});

Echoed.Router = Backbone.Router.extend({
    initialize: function(options) {
        _.bindAll(this, 'summary', 'rewards', 'settings', 'reports','echoes');
        this.page = null;
        this.EvAg = options.EvAg;
    },
    routes:{
        "summary": "summary",
        "": "summary",
        "echoes": "echoes",
        "rewards": "rewards",
        "settings": "settings",
        "reports": "reports",
        "reports/:report" : "reports",
        "reports/:report/:id" : "reports"
    },
    summary: function() {
        if(this.page != "summary")
            var pageView = new Echoed.Views.Pages.Summary({EvAg: this.EvAg});
        this.page ="summary";
    },
    rewards: function() {
        if(this.page != "rewards")
            var pageView = new Echoed.Views.Pages.Rewards({EvAg: this.EvAg});
        this.page = "rewards";
    },
    echoes: function() {
        if(this.page != "echoes")
            var pageView = new Echoed.Views.Pages.Echoes({EvAg: this.EvAg});
        this.page = "echoes";
    },
    settings: function() {
        if(this.page != "settings")
            var pageView = new Echoed.Views.Pages.Settings({EvAg: this.EvAg});
        this.page = "settings";
    },
    reports: function(report, id) {
        var newPage = "reports/" + report + "/" + id;
        if(this.page != newPage)
            var pageView = new Echoed.Views.Pages.Reports({EvAg: this.EvAg,report: report,reportId: id});
        this.EvAg.trigger('subnav/change',{subnav: report});
        this.page= newPage;
    }
});

Echoed.Views.Pages.Echoes = Backbone.View.extend({
    el: '#content',
    initialize: function(options){
        _.bindAll(this, 'render');
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.EvAg.trigger('title/change',{title: 'Echoes' });
        this.EvAg.trigger('page/change', {page: 'echoes' });
        this.render();
    },
    render: function(){
        var self = this;
        var template = _.template($('#template-view-echoes').html());
        self.element.html(template);
        $.ajax({
            url: Echoed.urls.api + "/partner/retailer/echoes",
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: function(data){
                var table = {"style":"report-table","header": [{"text":"Order Id"},{"text" : "Product Id"}, {"text" : "Product Name"}, {"text" : "Purchase Date"}, { "text" : "Credit"}, { "text" : "Visitors" }, { "text" : "Credit Window Ends At" } ],"rows": []};
                $.each(data, function(index, echo){
                    var row = {"href":"#", cells:[]};
                    var cDate = new Date(echo.createdOn);
                    var eDate = new Date(echo.creditWindowEndsAt);

                    row.cells.push({"text": echo.orderId, "style" : ""});
                    row.cells.push({"text": echo.productId, "style" : ""});
                    row.cells.push({"text": echo.productName, "style" : ""});
                    row.cells.push({"text": cDate.toDateString() , "style" : ""});
                    row.cells.push({"text": echo.credit , "style" : "number"});
                    row.cells.push({"text": echo.totalClicks, "style" : "number"});
                    row.cells.push({"text": eDate.toDateString() , "style" : ""});
                    table.rows.push(row);
                });
                var echoesTable = new Echoed.Views.Components.TableTemplate({EvAg: self.EvAg, el: "#echoes-table",table:table});
                self.element.fadeIn();
            }
        });
    }
});

Echoed.Views.Pages.Reports = Backbone.View.extend({
    el: '#content',
    initialize: function(options) {
        _.bindAll(this, 'render');
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.report = options.report;
        this.reportId = options.reportId;
        this.EvAg.trigger('title/change',{title: 'Reports'});
        this.EvAg.trigger('page/change',
            {
                page: 'reports',
                subNav: [
                    {
                        href: '#reports/customers',
                        id:   'customers',
                        text: 'Customers'
                    },
                    {
                        href: '#reports/products',
                        id:   'products',
                        text: 'Products'
                    }
                ]
            }
        );
        this.render();
    },
    render: function() {
        var self = this;
        var template = null;
        if(self.reportId){
            switch(self.report){
                case "customers":
                    template = _.template($('#template-view-reports-customers').html());
                    $.ajax({
                        url: Echoed.urls.api + "/partner/customers/" + self.reportId + "/summary",
                        dataType: 'json',
                        xhrFields: {
                            withCredentials: true
                        },
                        success: function(data){
                            self.element.hide().html(template).fadeIn(function(){
                                var reportChart = new Echoed.Views.Components.Chart({EvAg: this.EvAg, el: '#customer-report-chart', customerId: self.reportId, type: "customer"});
                                $('#select-report').html(data.echoedUserName);
                                $('#cs-likes').hide().html(data.totalFacebookLikes).fadeIn();
                                $('#cs-views').hide().html(data.totalEchoClicks).fadeIn();
                                $('#cs-retweets').hide().html(0).fadeIn();
                                $('#cs-echoes').hide().html(data.totalEchoes).fadeIn();
                                $('#cs-total').hide().html("$" + data.totalSales.toFixed(2)).fadeIn();
                                $('#cs-purchases').hide().html(data.totalSalesVolume).fadeIn();
                                $('#cs-comments').hide().html(data.totalFacebookComments).fadeIn();
                            });
                        }
                    });
                    break;
                case "products":
                    template = _.template($('#template-view-reports-products').html());
                    $.ajax({
                        url: Echoed.urls.api  +"/partner/products/" + self.reportId + "/summary",
                        dataType: 'json',
                        xhrFields: {
                            withCredentials: true
                        },
                        success: function(data){
                            self.element.hide().html(template).fadeIn(function(){
                                var reportChart = new Echoed.Views.Components.Chart({EvAg: this.EvAg, el: '#product-report-chart', productId: self.reportId, type: "product"});
                                $('#select-report').html(data.productName);
                                $('#ss-likes').hide().html(data.totalFacebookLikes).fadeIn();
                                $('#ss-views').hide().html(data.totalEchoClicks).fadeIn();
                                $('#ss-retweets').hide().html(0).fadeIn();
                                $('#ss-new-echoes').hide().html(data.totalEchoes).fadeIn();
                                $('#ss-open-echoes').hide().html(0).fadeIn();
                                $('#ss-comments').hide().html(data.totalFacebookComments).fadeIn();
                                $('#product-image').attr("src",data.productImageUrl);
                                $('#product-sku').html(data.productId);
                                $('#product-category').html(data.productCategory);
                                $('#product-brand').html(data.productBrand);
                            });
                        }
                    });
                    break;
            }
        }
        else{
            template = _.template($('#template-views-reports-table').html());
            switch(self.report){
                case "customers":
                    $.ajax({
                        url: Echoed.urls.api  + "/partner/reports/customers",
                        dataType: 'json',
                        xhrFields: {
                            withCredentials: true
                        },
                        success: function(data){
                            self.element.hide().html(template);
                            var table = {"style":"report-table","header": [{"text":"Customer"},{"text":"($)Purchases"},{"text":"(#)Purchases"},{"text":"Echoes"},{"text":"Page Views"},{"text":"Likes"},{"text":"Comments"}],"rows": []};
                            $.each(data.customers, function(index,customer){
                                var row = {"href":"#reports/customers/" + customer.echoedUserId , cells:[]};
                                if(customer.echoedUserName){
                                    row.cells.push({"text" : customer.echoedUserName, "style" : ""});
                                }
                                else {
                                    row.cells.push({"text" : "Guest", "style" : ""});
                                }
                                row.cells.push({"text" : "$" + customer.totalSales.toFixed(2), "style" : "number"});
                                row.cells.push({"text" : customer.totalSalesVolume, "style" : "number"});
                                row.cells.push({"text" : customer.totalEchoes, "style" : "number"});
                                row.cells.push({"text" : customer.totalEchoClicks, "style" : "number"});
                                row.cells.push({"text" : customer.totalFacebookLikes, "style" : "number"});
                                row.cells.push({"text" : customer.totalFacebookComments, "style" : "number"});
                                table.rows.push(row);
                            });
                            var customerTable = new Echoed.Views.Components.TableTemplate({EvAg: self.EvAg, el: "#table-container",table:table});
                            self.element.fadeIn();
                        }
                    });

                    break;
                case "products":
                    $.ajax({
                        url: Echoed.urls.api + "/partner/reports/products",
                        dataType: 'json',
                        xhrFields: {
                            withCredentials: true
                        },
                        success: function(data){
                            self.element.hide().html(template);
                            var table = {"style":"report-table","header": [{"text":"Product Name"},{"text":"Product Id"},{"text":"($)Purchases"},{"text":"(#)Purchases"},{"text":"Echoes"},{"text":"Page Views"},{"text":"Likes"},{"text":"Comments"}],"rows": []};
                            $.each(data.products, function(index,product){
                                var row = {"href":"#reports/products/" + product.productId , cells:[]};
                                row.cells.push({"text":product.productName, "style" : ""});
                                row.cells.push({"text":product.productId, "style" : ""});
                                row.cells.push({"text": "$" + product.totalSales.toFixed(2), "style" : "number"});
                                row.cells.push({"text":product.totalSalesVolume, "style": "number"});
                                row.cells.push({"text":product.totalEchoes, "style" : "number"});
                                row.cells.push({"text":product.totalEchoClicks, "style" : "number"});
                                row.cells.push({"text":product.totalFacebookLikes, "style" : "number"});
                                row.cells.push({"text":product.totalFacebookComments, "style" : "number"});
                                table.rows.push(row);
                            });
                            var productTable = new Echoed.Views.Components.TableTemplate({EvAg: this.EvAg, el: "#table-container", table:table});
                            self.element.fadeIn();
                        }
                    });
                    break;
                default:
                    self.element.html('');
                    break;
            }
        }
        return this;
    }

});

Echoed.Views.Pages.Settings = Backbone.View.extend({
    el: '#content',
    initialize: function(options) {
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.EvAg.trigger('title/change',{title: 'Echoed | Settings'});
        this.EvAg.trigger('page/change',{page: 'settings'});
        this.render();
    },
    render: function() {
        var template = _.template($('#template-view-settings').html());
        $(this.el).hide().html(template).fadeIn();
        return this;
    }
});

Echoed.Views.Pages.Rewards = Backbone.View.extend({
    initialize: function(options) {
        _.bindAll(this);
        this.el = $('#content');
        this.EvAg = options.EvAg;
        options = {
            page: 'rewards',
            subNav: [
                {
                    href:'#rewards/open',
                    id: 'open',
                    text: 'Open Echoes'
                },
                {
                    href:'#rewards/closed',
                    id: 'closed',
                    text: 'Processed Echoes'
                }
            ]

        };
        this.EvAg.trigger('title/change',{title: 'Echoed | Rewards'});
        this.EvAg.trigger('page/change',options);
        this.render();
    },
    render: function() {
        var template = _.template($('#template-view-rewards').html());
        this.el.hide().html(template).fadeIn();
        return this;
    }
});

Echoed.Views.Pages.Summary = Backbone.View.extend({
    el: '#content',
    initialize: function(options) {
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.EvAg.trigger('title/change',{title: 'Echoed | Summary'});


        this.EvAg.trigger('page/change', {page: 'summary'});
        this.render();
    },
    render: function() {
        var template = _.template($('#template-view-summary').html());
        $(this.el).hide().html(template).fadeIn(function() {
            $.ajax({
                url: Echoed.urls.api  + "/partner/reports/summary",
                dataType: 'json',
                xhrFields: {
                    withCredentials: true
                },
                success: function(data){
                    $('#ss-likes').hide().html(data.totalFacebookLikes).fadeIn();
                    $('#ss-echoes').hide().html(data.totalEchoes).fadeIn();
                    $('#ss-purchases').hide().html(data.totalSalesVolume).fadeIn();
                    $('#ss-visits').hide().html(data.totalEchoClicks).fadeIn();
                    $('#ss-comments').hide().html(data.totalFacebookComments).fadeIn();
                    $('#ss-salesamount').hide().html("$" + data.totalSales.toFixed(2)).fadeIn();
                },
                error:function (xhr, ajaxOptions, thrownError){
                }
            });
        });
        var chart = new Echoed.Views.Components.Chart({EvAg: this.EvAg, el: '#chart', type: "partner"});
        $.ajax({
            url: Echoed.urls.api + "/partner/reports/topproducts",
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: function(data){
                var table = {"style":"top-table","header": [{"text":"Product Name"},{"text":"Product Id"},{"text":"Echoes"},{"text":"(#) Sales"},{"text":"($) Sales"},{"text":"Page Views"},{"text":"Likes"},{"text":"Comments"}],"rows": []};
                $.each(data.products, function(index,product){
                    var row = {"href": "#reports/products/" + product.productId, cells:[] };
                    row.cells.push({"text" : product.productName, "style" : ""});
                    row.cells.push({"text" : product.productId, "style" : ""});
                    row.cells.push({"text" : product.totalSalesVolume, "style" : "number"});
                    row.cells.push({"text" : product.totalSales, "style" : "number"});
                    row.cells.push({"text" : product.totalEchoes, "style" : "number"});
                    row.cells.push({"text" : product.totalEchoClicks, "style" : "number"});
                    row.cells.push({"text" : product.totalFacebookLikes, "style" : "number"});
                    row.cells.push({"text" : product.totalFacebookComments, "style" : "number"});
                    table.rows.push(row);
                });
                var topProductsTable = new Echoed.Views.Components.TableTemplate({EvAg: self.EvAg, el:'#top-products-table', table:table});
            }
        });
        $.ajax({
            url: Echoed.urls.api  + "/partner/reports/geolocation",
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: function(data){
                var center = new google.maps.LatLng(0, 0);
                var options = {
                    'zoom' : 2,
                    'center' : center,
                    'mapTypeId' : google.maps.MapTypeId.ROADMAP
                };
                var map = new google.maps.Map(document.getElementById("map"), options);
                var markers= [];
                $.each(data, function(index, marker){
                    var marker = new google.maps.Marker({
                        position: new google.maps.LatLng(marker.latitude, marker.longitude),
                        map: map

                    });
                    markers.push(marker)

                });
                var markerCluster = new MarkerClusterer(map, markers);
            }
        });
        $.ajax({
            url: Echoed.urls.api  + "/partner/reports/topcustomers",
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: function(data){
                var table = {"style":"top-table","header": [{"text":"Customer"},{"text":"(#) Purchases"},{"text":"($) Purchases"},{"text":"Echoes"},{"text":"Page Views"},{"text":"Likes"},{"text":"Comments"}],"rows": []};
                $.each(data.customers, function(index,customer){
                    var row = {"href":"#reports/customers/" + customer.echoedUserId , cells:[]};
                    row.cells.push({"text":customer.echoedUserName, "style" : ""});
                    row.cells.push({"text" : customer.totalSalesVolume, "style" : "number"});
                    row.cells.push({"text" : customer.totalSales, "style" : "number"});
                    row.cells.push({"text" : customer.totalEchoes, "style" : "number"});
                    row.cells.push({"text" : customer.totalEchoClicks, "style" : "number"});
                    row.cells.push({"text" : customer.totalFacebookLikes, "style" : "number"});
                    row.cells.push({"text" : customer.totalFacebookComments, "style" : "number"});
                    table.rows.push(row);
                });
                var topCustomersTable = new Echoed.Views.Components.TableTemplate({EvAg: self.EvAg, el: '#top-customers-table', table: table})
            }
        });
        return this;
    }
});

Echoed.Views.Components.Title = Backbone.View.extend({
    el: 'title',
    initialize: function(options){
        _.bindAll(this,'changeTitle');
        this.EvAg = options.EvAg;
        this.EvAg.bind('title/change',this.changeTitle);
        this.element = $(this.el);
    },
    changeTitle: function(options){
        this.element.html(options.title);
    }
});

Echoed.Views.Components.TableTemplate = Backbone.View.extend({
    initialize: function(options){
        this.EvAg = options.EvAg;
        this.el = options.el;
        this.element = $(this.el);
        this.table = options.table;
        this.render();
    },
    events:{
        "click td": "triggerClick",
        "mouseenter tr": "highlight",
        "mouseleave tr": "removeHighlight"
    },
    render: function(){
        var self = this;
        //Generate Headers
        var table = $('<table></table>').appendTo(self.element).addClass(self.table.style);
        var thead = $('<thead></thead>').appendTo(table);
        $.each(self.table.header,function(index, th){
            var thEl = $('<th></th>').html(th.text).appendTo(thead);
        });
        thead.appendTo(table);
        var tbody = $('<tbody></tbody>').appendTo(table);
        $.each(self.table.rows, function(index,row){
            var tr = $('<tr></tr>').appendTo(tbody).attr("href",row.href);
            $.each(row.cells, function(index, cell){
                var td = $('<td></td>').html(cell.text).addClass(cell.style).appendTo(tr);
            });
        });
        table.appendTo(this.element);
    },
    triggerClick: function(e){
        document.location.href = $(e.target).parent().attr("href");
    },
    highlight: function(e){
        $(e.target).parent().css("background-color","#FF7733");
    },
    removeHighlight: function(e){
        $(e.target).parent().css("background-color","");
    }
});

Echoed.Views.Components.ProductSummary = Backbone.View.extend({
    initialize: function(options) {
        _.bindAll(this);
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.render();
    },
    render: function() {
        var self = this;
        var template = _.template($('#template-view-component-productsummary').html());
        self.element.hide().html(template).fadeIn(function() {
        });
    }
});

Echoed.Views.Components.RewardTable = Backbone.View.extend({
    initialize: function(options) {
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.render();
    },
    render: function() {
        var self = this;
        var template = _.template($("#template-view-component-rewardtable").html());
        self.element.html(template);
        return this;
    }
});

Echoed.Views.Components.Nav = Backbone.View.extend({
    el: '#header-nav',
    initialize: function(options) {
        _.bindAll(this, 'pageChange');
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind('page/change', this.pageChange);
        this.currentPage = null;
    },
    pageChange: function(options) {
        if (this.currentPage != options.page) {
            this.element.find(".current").removeClass("current");
            $('#' + options.page + "_nav").addClass("current");
            this.currentPage = options.page;
        }
    }
});

Echoed.Views.Components.SubNav = Backbone.View.extend({
    el: '#sub-nav',
    initialize: function(options) {
        _.bindAll(this, 'pageChange', 'slideDown', 'slideUp', 'select');
        this.element = $(this.el);
        this.EvAg = options.EvAg;
        this.EvAg.bind('page/change', this.pageChange);
        this.EvAg.bind('subnav/change', this.select);
        this.render();
    },
    render: function() {
        return this;
    },
    select: function(options) {
        this.element.find(".current").removeClass("current");
        $("#subnav-" + options.subnav).addClass("current");
    },
    pageChange: function(options) {
        var self = this;
        this.element.html('');
        if (options.subNav) {
            $(options.subNav).each(function(index, item) {
                $('<a></a>').attr("id", "subnav-" + item.id).attr('href', item.href).html(item.text).appendTo(self.element);
            });
            self.slideDown();
        }
        else {
            self.slideUp();
        }
    },
    slideDown: function() {
        this.element.slideDown('fast');
    },
    slideUp: function() {
        this.element.slideUp('fast');
    }
});

Echoed.Views.Components.DonutChart = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.chart = null;
        this.el = options.el;
        this.render();
    },
    render: function(){
        chart = new Highcharts.Chart(
            {
                chart: {
                    renderTo: ''
                },
                title: {
                    text: 'test'
                },
                plotOptions:{
                    pie: {
                        shadow: false
                    }
                },
                tooltip:{
                    formatter: function(){
                        return '<b>' + this.point.name + '</b>:' + this.y + ' %';
                    }
                },
                series: [{
                }]
            });
    }
});

Echoed.Views.Components.Comments = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
    },
    render: function(){

    }
});

Echoed.Views.Components.Chart = Backbone.View.extend({
    initialize: function(options) {
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.chart = null;
        this.el = options.el;
        var self = this;
        switch(options.type){
            case 'partner':
                self.url = Echoed.urls.api + "/partner/reports/history";
                break;
            case 'product':
                self.url = Echoed.urls.api + "/partner/products/" + options.productId +"/history";
                break;
            case 'customer':
                self.url = Echoed.urls.api + "/partner/customers/" + options.customerId +  "/history";
                break;
        }
        self.series = [];
        $.ajax({
            url: self.url,
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: function(data){
                $.each(data.series, function(index,activity){
                    var chartData = { name: activity.name, data: []};
                    if (chartData.name == "Sales($)"){
                        chartData.yAxis = 1;
                    }
                    if(activity.data.length > 0){
                        $.each(activity.data, function(index,history){
                            var a = [];
                            a.push(history.date);
                            a.push(history.count);
                            chartData.data.push(a);
                        });
                        self.series.push(chartData)
                    }
                });
                self.render();
            }
        });

    },
    render: function() {
        var self = this;
        var chartOptions = {
            chart: {
                renderTo: this.el.substr(1),
                defaultSeriesType: 'spline',
                stacked: true,
                zoomType: 'x'

            },
            credits: {
                text: '',
                href: ''
            },
            colors: ['#3B5998', '#09F','#497593','#EE7402','#6A971F','#C6014A','#0274B8','#F0B500','#92ACBE'],
            title: { text: ''},
            yAxis:[{
                min: 0,
                title:{
                    text: 'Activity',
                    style: {
                        fontWeight: 'normal',
                        color: '#666666',
                        fontFamily:'"Helvetica Neue",Helvetica,Arial',
                        fontSize: '11px'
                    }
                }
            },{
                min: 0,
                title:{
                    text: '($) Purchases',
                    style: {
                        fontWeight: 'normal',
                        color: '#666666',
                        fontFamily:'"Helvetica Neue",Helvetica,Arial',
                        fontSize: '11px'
                    }
                },
                opposite: true
            }],
            xAxis: {
                type: 'datetime',
                dateTimeLabelFormats:{
                    second: "%b %e - %H:%M",
                    minute: "%b %e - %H:%M",
                    hour: "%b %e - %H:%M"
                }
            },
            tooltip: {
                formatter: function() {
                    return '<b>' +Highcharts.dateFormat('%b %e', this.x) +':</b><br/> '+ this.y +' '+ this.series.name;
                }
            },
            series: self.series
        };
        this.chart = new Highcharts.Chart(chartOptions);
    }
});
