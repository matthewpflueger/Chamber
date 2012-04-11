window.onload = function(){
    var _body = document.getElementsByTagName('body')[0];
    loadJSInclude('https://crypto-js.googlecode.com/files/2.5.3-crypto-sha1-hmac-pbkdf2-blockmodes-aes.js',function(){
            loadJSInclude('http://demo.echoed.com/js/rawdeflate.js', function(){
                    loadJSInclude('http://demo.echoed.com/js/jquery-1.6.2.min.js', function(){
                        var string = getEchoedJsonString();

                        var secret = Crypto.util.base64ToBytes(EchoedKey);
                        var iv = Crypto.charenc.Binary.stringToBytes("1234567890123456");

                        var content = Crypto.util.bytesToBase64(Crypto.charenc.Binary.stringToBytes(RawDeflate.deflate(string)));

                        var crypted = Crypto.AES.encrypt(content ,secret,{ mode: new Crypto.mode.CBC(Crypto.pad.NoPadding), iv: iv });
                        crypted = crypted.replace(/\+/g,"-");
                        crypted = crypted.replace(/\//g,"_");
                        console.log("Encrypted: " + crypted);

                        var script = document.createElement("script");
                        script.type = "text/javascript";
                        script.innerHTML = 'echoData = "' + crypted + '"';
                        _body.appendChild(script);
                        var requestScript = document.createElement("script");
                        requestScript.type = "text/javascript";
                        requestScript.src = 'http://localhost.local:8080/echo/js?pid=' + EchoedPartnerId;
                        _body.appendChild(requestScript);
            })
        })
    });
}

function loadJSInclude(scriptPath, callback){
    var scriptNode = document.createElement("script");
    scriptNode.type ="text/javascript";
    scriptNode.src = scriptPath;
    var head = document.getElementsByTagName('HEAD')[0];
    head.appendChild(scriptNode)
    if(callback != null) {
        scriptNode.onreadystatechange = callback;
        scriptNode.onload = callback;
    }
}

function getQueryVariable(string, variable){
    var vars = string.split("&");
    for ( var i = 0; i < vars.length; i++){
        var pair = vars[i].split("=");
        if(pair[0]== variable){
            return decodeURIComponent(pair[1]);
        }
    }
    return null;
}

function getEchoedJsonString(){
    var orderId = "";
    var customerId = "";
    var boughtOn = "";
    var partnerId = "";
    var items = [];
    $('img[src^="http://demo.echoed.com/echo/button"]').each(function(){
        var item = {};
        var pair = $(this).attr("src").split("?");
        var queryString = pair[1];
        if(orderId = "") {
            partnerId = getQueryVariable(queryString, "partnerId");
            orderId = getQueryVariable(queryString, "orderId");
            customerId = getQueryVariable(queryString, "customerId");
            boughtOn = getQueryVariable(queryString, "boughtOn");
        }
        item['productId'] = getQueryVariable(queryString, 'productId');
        item['productName'] = getQueryVariable(queryString, 'productName');
        item['category'] = getQueryVariable(queryString, 'category');
        item['brand'] = getQueryVariable(queryString,'brand');
        item['price'] = getQueryVariable(queryString, 'price');
        item['imageUrl'] = getQueryVariable(queryString, 'imageUrl');
        item['landingPageUrl'] = getQueryVariable(queryString, 'landingPageUrl');
        item['description'] = getQueryVariable(queryString, 'description');
        items.push(item);
    });
    var request = {};
    request['orderId'] = orderId;
    request['customerId'] = customerId;
    request['boughtOn'] = boughtOn;
    request['items'] = items;
    return JSON.stringify(request);
}