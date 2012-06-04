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
            self.element.append($('<option></option>').attr("value", row.value).html(row.text));
        });
    },
    onchange: function(){
        var self = this;
        var optionText = self.element.find("option:selected").text();
        var optionValue = self.element.val();
        self.EvAg.trigger("select/change", optionText, optionValue);
    }
});

Echoed.Views.Pages.Partners = Backbone.View.extend({
    el: '#content',
    initialize: function(options){
        _.bindAll(this);
        this.EvAg = options.EvAg;
        this.element = $(this.el);
        this.EvAg.bind('select/change', this.renderPartnerSettings);
        this.render();
    },
    events: {
        "click .partner-setting-submit" : "updatePartnerSettings"
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
    updatePartnerSettings: function(){
        var form = $('#partner-settings-add');
        var serializedString = form.serialize();
        var v = $('#partnerId').val();
        alert(serializedString);
        $.ajax({
            url: Echoed.urls.api + "/admin/partners/" + v + "/settings/update",
            data: form.serialize(),
            type: 'POST',
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: function(data){
                alert('success');
            }
        })
    },
    renderPartnerSettings: function(t, v){
        $.ajax({
            url: Echoed.urls.api + "/admin/partners/" + v + "/settings",
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: function(data){

                var table = {"style" : "report-table" ,
                    "header" : [
                        { "text": "Setting Id"},
                        { "text": "Partner Id"},
                        { "text": "Share %"},
                        { "text": "Min Clicks"},
                        { "text": "Min %"},
                        { "text": "Max Clicks"},
                        { "text": "Max %"},
                        { "text": "Views"},
                        { "text": "@ Handle"},
                        { "text": "@ ActiveDate"}
                    ],
                    "rows" : []};
                $.each(data, function(index, partnerSettings){
                    var row = { "href":"#", cells:[]};
                    row.cells.push({"text" : partnerSettings.id});
                    row.cells.push({"text" : partnerSettings.partnerId });
                    row.cells.push({"text" : partnerSettings.closetPercentage });
                    row.cells.push({"text" : partnerSettings.minClicks });
                    row.cells.push({"text" : partnerSettings.minPercentage });
                    row.cells.push({"text" : partnerSettings.maxClicks });
                    row.cells.push({"text" : partnerSettings.maxPercentage });
                    row.cells.push({"text" : partnerSettings.views });
                    row.cells.push({"text" : partnerSettings.hashtag ? partnerSettings.hashtag: ""});
                    row.cells.push({"text" : partnerSettings.activeOn });
                    table.rows.push(row);
                });
                $('#partner-settings-container').empty();
                var settingsTable = new Echoed.Views.Components.TableTemplate({ EvAg:self.EvAg, el:"#partner-settings-container", table:table})
                $('#partnerName').val(t);
                $('#partnerId').val(v);
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
                var td = $('<td></td>').html(cell.text).addClass(cell.style ? cell.style : "").appendTo(tr);
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

