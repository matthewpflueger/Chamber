if (!('Echoed' in this)) this.Echoed = {};

$(document).ready(function () {
    Echoed.init();
});

var EventAggregator = _.extend({}, Backbone.Events);

Echoed = {
    Views:{
        Pages:{},
        Components:{}
    },
    Models:{},
    init:function () {
        var router = new Echoed.Router({EvAg:EventAggregator});
        Backbone.history.start();
    }
};

Echoed.Models.Page = Backbone.Model.extend({
    defaults:{
        title:'Summary',
        currentNav:'summary_nav'
    },
    initialize:function () {
    }
});

Echoed.Views.Components.Select = Backbone.View.extend({
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(options.el);
        this.render(options.rows);
    },
    events: {
        'change': 'onchange'
    },
    render: function(rows){
        var self = this;
        self.element.html('');
        $.each(rows, function(index, row){
            self.element.append($('<option></option>').attr("value",row.value).html(row.text));
        });
    },
    onchange: function(){
        var self = this;
        self.EvAg.trigger("select/change", self.element.val());
    }
});

Echoed.Views.Pages.Partners = Backbone.View.extend({
    el: '#content',
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.render();
    },
    render: function(){
        var self = this;
        var template = _.template($('#templates-pages-partners').html());
        self.element.html(template);
        $.ajax({
            url: Echoed.urls.api + "/admin/partners",
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: function(partners){
                var rows = [];
                $.each(partners, function(index, partner){
                    var row = { };
                    row.text = partner.name;
                    row.value = partner.id;
                    rows.push(row);
                });
                var partnerDropdown = new Echoed.Views.Components.Select({ el: '#partners', EvAg: self.EvAg, rows: rows });
            }
        })
    },
    renderPartnerSettings: function(e){
        $.ajax({
            url: Echoed.urls.api + "/admin/partners/" + e + "/settings",
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: function(data){
                alert('test');
            }
        })
    }
});

Echoed.Router = Backbone.Router.extend({
    initialize:function (options) {
        _.bindAll(this, 'summary');
        this.page = null;
        this.EvAg = options.EvAg;
    },
    routes:{
        "": "summary",
        "summary":"summary",
        "partners":"partners"
    },
    summary: function(){
        var pageView = new Echoed.Views.Pages.Summary({EvAg:this.EvAg});
        this.page = "summary";
    },
    partners: function(){
        var pageView = new Echoed.Views.Pages.Partners({EvAg:this.EvAg});
        this.page = "partners";
    }
});

Echoed.Views.Pages.Summary = Backbone.View.extend({
    el:'#content',
    initialize:function (options) {
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.render();
    },
    render:function () {
        var self = this;
        var template = _.template($('#templates-pages-summary').html());
        self.element.html(template);
        $.ajax({
            url:Echoed.urls.api + "/admin/users",
            dataType:'json',
            xhrFields:{
                withCredentials:true
            },
            success:function (users) {
                var table = {"style":"report-table", "header":[
                    {"text":"Name"},
                    {"text":"Created On"},
                    {"text":"Email"}
                ], "rows":[]};
                $.each(users, function (index, user) {
                    var row = { "href":"#", cells:[]};
                    var date = new Date(user.createdOn);
                    row.cells.push({"text":user.name});
                    row.cells.push({"text":date.toDateString()});
                    row.cells.push({"text":user.email });
                    table.rows.push(row);
                });
                var userTable = new Echoed.Views.Components.TableTemplate({ EvAg:self.EvAg, el:"#echoed-users", table:table})
            }
        });
        $.ajax({
            url:Echoed.urls.api + "/admin/echoPossibility",
            dataType:'json',
            xhrFields:{
                withCredentials:true
            },
            success:function (echoPossibilities) {
                var table = {"style":"report-table", "header":[
                    {"text":"Date"},
                    {"text":"Partner Id"},
                    {"text":"Order Id"},
                    {"text":"Product Id"},
                    {"text":"Price"},
                    {"text":"view"},
                    {"text":"step"}
                ], rows:[]};
                $.each(echoPossibilities, function (index, echoPossibility) {
                    var row = { "href":"#", cells:[] };
                    var date = new Date(echoPossibility.createdOn);
                    row.cells.push({"text":date.toDateString()});
                    row.cells.push({"text":echoPossibility.partnerId});
                    row.cells.push({"text":echoPossibility.orderId});
                    row.cells.push({"text":echoPossibility.productId});
                    row.cells.push({"text":echoPossibility.price});
                    row.cells.push({"text":echoPossibility.view});
                    row.cells.push({"text":echoPossibility.step});
                    table.rows.push(row);
                });
                var echoPossibilityTable = new Echoed.Views.Components.TableTemplate({ EvAg:self.EvAg, el:"#echoes", table:table})
            }
        });

    }
});

Echoed.Views.Components.Title = Backbone.View.extend({
    el:'title',
    initialize:function (options) {
        _.bindAll(this, 'changeTitle');
        this.EvAg = options.EvAg;
        this.EvAg.bind('title/change', this.changeTitle);
        this.element = $(this.el);
    },
    changeTitle:function (options) {
        this.element.html(options.title);
    }
});

Echoed.Views.Components.TableTemplate = Backbone.View.extend({
    initialize:function (options) {
        this.EvAg = options.EvAg;
        this.el = options.el;
        this.element = $(this.el);
        this.table = options.table;
        this.render();
    },
    events:{
        "click td":"triggerClick",
        "mouseenter tr":"highlight",
        "mouseleave tr":"removeHighlight"
    },
    render:function () {
        var self = this;
        //Generate Headers
        var table = $('<table></table>').appendTo(self.element).addClass(self.table.style);
        var thead = $('<thead></thead>').appendTo(table);
        $.each(self.table.header, function (index, th) {
            var thEl = $('<th></th>').html(th.text).appendTo(thead);
        });
        thead.appendTo(table);
        var tbody = $('<tbody></tbody>').appendTo(table);
        $.each(self.table.rows, function (index, row) {
            var tr = $('<tr></tr>').appendTo(tbody).attr("href", row.href);
            $.each(row.cells, function (index, cell) {
                var td = $('<td></td>').html(cell.text).addClass(cell.style).appendTo(tr);
            });
        });
        table.appendTo(this.element);
    },
    triggerClick:function (e) {
        document.location.href = $(e.target).parent().attr("href");
    },
    highlight:function (e) {
        $(e.target).parent().css("background-color", "#FF7733");
    },
    removeHighlight:function (e) {
        $(e.target).parent().css("background-color", "");
    }
});

