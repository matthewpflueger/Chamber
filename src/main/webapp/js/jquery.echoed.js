window.onload = function(){
    var _body = document.getElementsByTagName('body')[0];
    loadJSInclude('https://crypto-js.googlecode.com/files/2.5.3-crypto-sha1-hmac-pbkdf2-blockmodes-aes.js',function(){
        loadJSInclude('https://c779203.ssl.cf2.rackcdn.com/rawdeflate.js', function(){
            loadJSInclude('https://c779203.ssl.cf2.rackcdn.com/jquery-1.6.2.min.js', function(){
                if(EchoedBaseUrl == undefined){
                    EchoedBaseUrl = "https://www.echoed.com";
                }



                var string = JSON.stringify(echoedRequest);
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
                requestScript.src = EchoedBaseUrl + '/echo/js?pid=' + EchoedPartnerId;
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
    head.appendChild(scriptNode);
    if(callback != null) {
        scriptNode.onreadystatechange = callback;
        scriptNode.onload = callback;
    }
}