/*
 * Crypto-JS v2.5.3
 * http://code.google.com/p/crypto-js/
 * (c) 2009-2012 by Jeff Mott. All rights reserved.
 * http://code.google.com/p/crypto-js/wiki/License
 */
(typeof Crypto=="undefined"||!Crypto.util)&&function(){var c=window.Crypto={},j=c.util={rotl:function(e,d){return e<<d|e>>>32-d},rotr:function(e,d){return e<<32-d|e>>>d},endian:function(e){if(e.constructor==Number)return j.rotl(e,8)&16711935|j.rotl(e,24)&4278255360;for(var d=0;d<e.length;d++)e[d]=j.endian(e[d]);return e},randomBytes:function(e){for(var d=[];e>0;e--)d.push(Math.floor(Math.random()*256));return d},bytesToWords:function(e){for(var d=[],b=0,a=0;b<e.length;b++,a+=8)d[a>>>5]|=(e[b]&255)<<
24-a%32;return d},wordsToBytes:function(e){for(var d=[],b=0;b<e.length*32;b+=8)d.push(e[b>>>5]>>>24-b%32&255);return d},bytesToHex:function(e){for(var d=[],b=0;b<e.length;b++)d.push((e[b]>>>4).toString(16)),d.push((e[b]&15).toString(16));return d.join("")},hexToBytes:function(e){for(var d=[],b=0;b<e.length;b+=2)d.push(parseInt(e.substr(b,2),16));return d},bytesToBase64:function(e){if(typeof btoa=="function")return btoa(f.bytesToString(e));for(var d=[],b=0;b<e.length;b+=3)for(var a=e[b]<<16|e[b+1]<<
8|e[b+2],k=0;k<4;k++)b*8+k*6<=e.length*8?d.push("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(a>>>6*(3-k)&63)):d.push("=");return d.join("")},base64ToBytes:function(e){if(typeof atob=="function")return f.stringToBytes(atob(e));for(var e=e.replace(/[^A-Z0-9+\/]/ig,""),d=[],b=0,a=0;b<e.length;a=++b%4)a!=0&&d.push(("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(e.charAt(b-1))&Math.pow(2,-2*a+8)-1)<<a*2|"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(e.charAt(b))>>>
6-a*2);return d}},c=c.charenc={};c.UTF8={stringToBytes:function(e){return f.stringToBytes(unescape(encodeURIComponent(e)))},bytesToString:function(e){return decodeURIComponent(escape(f.bytesToString(e)))}};var f=c.Binary={stringToBytes:function(e){for(var d=[],b=0;b<e.length;b++)d.push(e.charCodeAt(b)&255);return d},bytesToString:function(e){for(var d=[],b=0;b<e.length;b++)d.push(String.fromCharCode(e[b]));return d.join("")}}}();
(function(){var c=Crypto,j=c.util,f=c.charenc,e=f.UTF8,d=f.Binary,b=c.SHA1=function(a,k){var e=j.wordsToBytes(b._sha1(a));return k&&k.asBytes?e:k&&k.asString?d.bytesToString(e):j.bytesToHex(e)};b._sha1=function(a){a.constructor==String&&(a=e.stringToBytes(a));var b=j.bytesToWords(a),l=a.length*8,a=[],d=1732584193,h=-271733879,c=-1732584194,f=271733878,s=-1009589776;b[l>>5]|=128<<24-l%32;b[(l+64>>>9<<4)+15]=l;for(l=0;l<b.length;l+=16){for(var g=d,n=h,r=c,m=f,o=s,p=0;p<80;p++){if(p<16)a[p]=b[l+p];else{var v=
a[p-3]^a[p-8]^a[p-14]^a[p-16];a[p]=v<<1|v>>>31}v=(d<<5|d>>>27)+s+(a[p]>>>0)+(p<20?(h&c|~h&f)+1518500249:p<40?(h^c^f)+1859775393:p<60?(h&c|h&f|c&f)-1894007588:(h^c^f)-899497514);s=f;f=c;c=h<<30|h>>>2;h=d;d=v}d+=g;h+=n;c+=r;f+=m;s+=o}return[d,h,c,f,s]};b._blocksize=16;b._digestsize=20})();
(function(){var c=Crypto,j=c.util,f=c.charenc,e=f.UTF8,d=f.Binary;c.HMAC=function(b,a,k,l){a.constructor==String&&(a=e.stringToBytes(a));k.constructor==String&&(k=e.stringToBytes(k));k.length>b._blocksize*4&&(k=b(k,{asBytes:!0}));for(var c=k.slice(0),k=k.slice(0),h=0;h<b._blocksize*4;h++)c[h]^=92,k[h]^=54;b=b(c.concat(b(k.concat(a),{asBytes:!0})),{asBytes:!0});return l&&l.asBytes?b:l&&l.asString?d.bytesToString(b):j.bytesToHex(b)}})();
(function(){var c=Crypto,j=c.util,f=c.charenc,e=f.UTF8,d=f.Binary;c.PBKDF2=function(b,a,k,l){function u(b,a){return c.HMAC(h,a,b,{asBytes:!0})}b.constructor==String&&(b=e.stringToBytes(b));a.constructor==String&&(a=e.stringToBytes(a));for(var h=l&&l.hasher||c.SHA1,f=l&&l.iterations||1,q=[],s=1;q.length<k;){for(var g=u(b,a.concat(j.wordsToBytes([s]))),n=g,r=1;r<f;r++)for(var n=u(b,n),m=0;m<g.length;m++)g[m]^=n[m];q=q.concat(g);s++}q.length=k;return l&&l.asBytes?q:l&&l.asString?d.bytesToString(q):j.bytesToHex(q)}})();
(function(c){function j(b,a){var k=b._blocksize*4;return k-a.length%k}var f=c.pad={},e=function(b){for(var a=b.pop(),k=1;k<a;k++)b.pop()};f.NoPadding={pad:function(){},unpad:function(){}};f.ZeroPadding={pad:function(b,a){var k=b._blocksize*4,e=a.length%k;if(e!=0)for(e=k-e;e>0;e--)a.push(0)},unpad:function(){}};f.iso7816={pad:function(b,a){var e=j(b,a);for(a.push(128);e>1;e--)a.push(0)},unpad:function(b){for(;b.pop()!=128;);}};f.ansix923={pad:function(b,a){for(var e=j(b,a),d=1;d<e;d++)a.push(0);a.push(e)},
unpad:e};f.iso10126={pad:function(b,a){for(var e=j(b,a),d=1;d<e;d++)a.push(Math.floor(Math.random()*256));a.push(e)},unpad:e};f.pkcs7={pad:function(b,a){for(var e=j(b,a),d=0;d<e;d++)a.push(e)},unpad:e};var c=c.mode={},d=c.Mode=function(b){if(b)this._padding=b};d.prototype={encrypt:function(b,a,e){this._padding.pad(b,a);this._doEncrypt(b,a,e)},decrypt:function(b,a,e){this._doDecrypt(b,a,e);this._padding.unpad(a)},_padding:f.iso7816};e=(c.ECB=function(){d.apply(this,arguments)}).prototype=new d;e._doEncrypt=
function(b,a){for(var e=b._blocksize*4,d=0;d<a.length;d+=e)b._encryptblock(a,d)};e._doDecrypt=function(b,a){for(var e=b._blocksize*4,d=0;d<a.length;d+=e)b._decryptblock(a,d)};e.fixOptions=function(b){b.iv=[]};e=(c.CBC=function(){d.apply(this,arguments)}).prototype=new d;e._doEncrypt=function(b,a,e){for(var d=b._blocksize*4,c=0;c<a.length;c+=d){if(c==0)for(var h=0;h<d;h++)a[h]^=e[h];else for(h=0;h<d;h++)a[c+h]^=a[c+h-d];b._encryptblock(a,c)}};e._doDecrypt=function(b,a,e){for(var d=b._blocksize*4,c=
0;c<a.length;c+=d){var h=a.slice(c,c+d);b._decryptblock(a,c);for(var f=0;f<d;f++)a[c+f]^=e[f];e=h}};e=(c.CFB=function(){d.apply(this,arguments)}).prototype=new d;e._padding=f.NoPadding;e._doEncrypt=function(b,e,d){for(var c=b._blocksize*4,d=d.slice(0),f=0;f<e.length;f++){var h=f%c;h==0&&b._encryptblock(d,0);e[f]^=d[h];d[h]=e[f]}};e._doDecrypt=function(e,a,d){for(var c=e._blocksize*4,d=d.slice(0),f=0;f<a.length;f++){var h=f%c;h==0&&e._encryptblock(d,0);var j=a[f];a[f]^=d[h];d[h]=j}};e=(c.OFB=function(){d.apply(this,
arguments)}).prototype=new d;e._padding=f.NoPadding;e._doEncrypt=function(e,d,c){for(var f=e._blocksize*4,c=c.slice(0),j=0;j<d.length;j++)j%f==0&&e._encryptblock(c,0),d[j]^=c[j%f]};e._doDecrypt=e._doEncrypt;c=(c.CTR=function(){d.apply(this,arguments)}).prototype=new d;c._padding=f.NoPadding;c._doEncrypt=function(e,d,c){for(var f=e._blocksize*4,c=c.slice(0),j=0;j<d.length;){var h=c.slice(0);e._encryptblock(h,0);for(var t=0;j<d.length&&t<f;t++,j++)d[j]^=h[t];++c[f-1]==256&&(c[f-1]=0,++c[f-2]==256&&
(c[f-2]=0,++c[f-3]==256&&(c[f-3]=0,++c[f-4])))}};c._doDecrypt=c._doEncrypt})(Crypto);
(function(){function c(e,d){for(var b=0,a=0;a<8;a++){d&1&&(b^=e);var c=e&128,e=e<<1&255;c&&(e^=27);d>>>=1}return b}for(var j=Crypto,f=j.util,e=j.charenc.UTF8,d=[99,124,119,123,242,107,111,197,48,1,103,43,254,215,171,118,202,130,201,125,250,89,71,240,173,212,162,175,156,164,114,192,183,253,147,38,54,63,247,204,52,165,229,241,113,216,49,21,4,199,35,195,24,150,5,154,7,18,128,226,235,39,178,117,9,131,44,26,27,110,90,160,82,59,214,179,41,227,47,132,83,209,0,237,32,252,177,91,106,203,190,57,74,76,88,207,
208,239,170,251,67,77,51,133,69,249,2,127,80,60,159,168,81,163,64,143,146,157,56,245,188,182,218,33,16,255,243,210,205,12,19,236,95,151,68,23,196,167,126,61,100,93,25,115,96,129,79,220,34,42,144,136,70,238,184,20,222,94,11,219,224,50,58,10,73,6,36,92,194,211,172,98,145,149,228,121,231,200,55,109,141,213,78,169,108,86,244,234,101,122,174,8,186,120,37,46,28,166,180,198,232,221,116,31,75,189,139,138,112,62,181,102,72,3,246,14,97,53,87,185,134,193,29,158,225,248,152,17,105,217,142,148,155,30,135,233,
206,85,40,223,140,161,137,13,191,230,66,104,65,153,45,15,176,84,187,22],b=[],a=0;a<256;a++)b[d[a]]=a;for(var k=[],l=[],u=[],h=[],t=[],q=[],a=0;a<256;a++)k[a]=c(a,2),l[a]=c(a,3),u[a]=c(a,9),h[a]=c(a,11),t[a]=c(a,13),q[a]=c(a,14);var s=[0,1,2,4,8,16,32,64,128,27,54],g=[[],[],[],[]],n,r,m,o=j.AES={encrypt:function(d,b,a){var a=a||{},i=a.mode||new j.mode.OFB;i.fixOptions&&i.fixOptions(a);var d=d.constructor==String?e.stringToBytes(d):d,c=a.iv||f.randomBytes(o._blocksize*4),b=b.constructor==String?j.PBKDF2(b,
c,32,{asBytes:!0}):b;o._init(b);i.encrypt(o,d,c);d=a.iv?d:c.concat(d);return a&&a.asBytes?d:f.bytesToBase64(d)},decrypt:function(d,b,a){var a=a||{},i=a.mode||new j.mode.OFB;i.fixOptions&&i.fixOptions(a);var d=d.constructor==String?f.base64ToBytes(d):d,c=a.iv||d.splice(0,o._blocksize*4),b=b.constructor==String?j.PBKDF2(b,c,32,{asBytes:!0}):b;o._init(b);i.decrypt(o,d,c);return a&&a.asBytes?d:e.bytesToString(d)},_blocksize:4,_encryptblock:function(e,a){for(var b=0;b<o._blocksize;b++)for(var i=0;i<4;i++)g[b][i]=
e[a+i*4+b];for(b=0;b<4;b++)for(i=0;i<4;i++)g[b][i]^=m[i][b];for(var c=1;c<r;c++){for(b=0;b<4;b++)for(i=0;i<4;i++)g[b][i]=d[g[b][i]];g[1].push(g[1].shift());g[2].push(g[2].shift());g[2].push(g[2].shift());g[3].unshift(g[3].pop());for(i=0;i<4;i++){var b=g[0][i],f=g[1][i],h=g[2][i],j=g[3][i];g[0][i]=k[b]^l[f]^h^j;g[1][i]=b^k[f]^l[h]^j;g[2][i]=b^f^k[h]^l[j];g[3][i]=l[b]^f^h^k[j]}for(b=0;b<4;b++)for(i=0;i<4;i++)g[b][i]^=m[c*4+i][b]}for(b=0;b<4;b++)for(i=0;i<4;i++)g[b][i]=d[g[b][i]];g[1].push(g[1].shift());
g[2].push(g[2].shift());g[2].push(g[2].shift());g[3].unshift(g[3].pop());for(b=0;b<4;b++)for(i=0;i<4;i++)g[b][i]^=m[r*4+i][b];for(b=0;b<o._blocksize;b++)for(i=0;i<4;i++)e[a+i*4+b]=g[b][i]},_decryptblock:function(d,e){for(var a=0;a<o._blocksize;a++)for(var c=0;c<4;c++)g[a][c]=d[e+c*4+a];for(a=0;a<4;a++)for(c=0;c<4;c++)g[a][c]^=m[r*4+c][a];for(var f=1;f<r;f++){g[1].unshift(g[1].pop());g[2].push(g[2].shift());g[2].push(g[2].shift());g[3].push(g[3].shift());for(a=0;a<4;a++)for(c=0;c<4;c++)g[a][c]=b[g[a][c]];
for(a=0;a<4;a++)for(c=0;c<4;c++)g[a][c]^=m[(r-f)*4+c][a];for(c=0;c<4;c++){var a=g[0][c],j=g[1][c],k=g[2][c],l=g[3][c];g[0][c]=q[a]^h[j]^t[k]^u[l];g[1][c]=u[a]^q[j]^h[k]^t[l];g[2][c]=t[a]^u[j]^q[k]^h[l];g[3][c]=h[a]^t[j]^u[k]^q[l]}}g[1].unshift(g[1].pop());g[2].push(g[2].shift());g[2].push(g[2].shift());g[3].push(g[3].shift());for(a=0;a<4;a++)for(c=0;c<4;c++)g[a][c]=b[g[a][c]];for(a=0;a<4;a++)for(c=0;c<4;c++)g[a][c]^=m[c][a];for(a=0;a<o._blocksize;a++)for(c=0;c<4;c++)d[e+c*4+a]=g[a][c]},_init:function(a){n=
a.length/4;r=n+6;o._keyexpansion(a)},_keyexpansion:function(a){m=[];for(var b=0;b<n;b++)m[b]=[a[b*4],a[b*4+1],a[b*4+2],a[b*4+3]];for(b=n;b<o._blocksize*(r+1);b++)a=[m[b-1][0],m[b-1][1],m[b-1][2],m[b-1][3]],b%n==0?(a.push(a.shift()),a[0]=d[a[0]],a[1]=d[a[1]],a[2]=d[a[2]],a[3]=d[a[3]],a[0]^=s[b/n]):n>6&&b%n==4&&(a[0]=d[a[0]],a[1]=d[a[1]],a[2]=d[a[2]],a[3]=d[a[3]]),m[b]=[m[b-n][0]^a[0],m[b-n][1]^a[1],m[b-n][2]^a[2],m[b-n][3]^a[3]]}}})();




/*
 * $Id: rawdeflate.js,v 0.3 2009/03/01 19:05:05 dankogai Exp dankogai $
 *
 * Original:
 *   http://www.onicos.com/staff/iz/amuse/javascript/expert/deflate.txt
 */

(function(){

    /* Copyright (C) 1999 Masanao Izumo <iz@onicos.co.jp>
     * Version: 1.0.1
     * LastModified: Dec 25 1999
     */

    /* Interface:
     * data = zip_deflate(src);
     */

    /* constant parameters */
    var zip_WSIZE = 32768;		// Sliding Window size
    var zip_STORED_BLOCK = 0;
    var zip_STATIC_TREES = 1;
    var zip_DYN_TREES    = 2;

    /* for deflate */
    var zip_DEFAULT_LEVEL = 6;
    var zip_FULL_SEARCH = true;
    var zip_INBUFSIZ = 32768;	// Input buffer size
    var zip_INBUF_EXTRA = 64;	// Extra buffer
    var zip_OUTBUFSIZ = 1024 * 8;
    var zip_window_size = 2 * zip_WSIZE;
    var zip_MIN_MATCH = 3;
    var zip_MAX_MATCH = 258;
    var zip_BITS = 16;
// for SMALL_MEM
    var zip_LIT_BUFSIZE = 0x2000;
    var zip_HASH_BITS = 13;
// for MEDIUM_MEM
// var zip_LIT_BUFSIZE = 0x4000;
// var zip_HASH_BITS = 14;
// for BIG_MEM
// var zip_LIT_BUFSIZE = 0x8000;
// var zip_HASH_BITS = 15;
    if(zip_LIT_BUFSIZE > zip_INBUFSIZ)
        alert("error: zip_INBUFSIZ is too small");
    if((zip_WSIZE<<1) > (1<<zip_BITS))
        alert("error: zip_WSIZE is too large");
    if(zip_HASH_BITS > zip_BITS-1)
        alert("error: zip_HASH_BITS is too large");
    if(zip_HASH_BITS < 8 || zip_MAX_MATCH != 258)
        alert("error: Code too clever");
    var zip_DIST_BUFSIZE = zip_LIT_BUFSIZE;
    var zip_HASH_SIZE = 1 << zip_HASH_BITS;
    var zip_HASH_MASK = zip_HASH_SIZE - 1;
    var zip_WMASK = zip_WSIZE - 1;
    var zip_NIL = 0; // Tail of hash chains
    var zip_TOO_FAR = 4096;
    var zip_MIN_LOOKAHEAD = zip_MAX_MATCH + zip_MIN_MATCH + 1;
    var zip_MAX_DIST = zip_WSIZE - zip_MIN_LOOKAHEAD;
    var zip_SMALLEST = 1;
    var zip_MAX_BITS = 15;
    var zip_MAX_BL_BITS = 7;
    var zip_LENGTH_CODES = 29;
    var zip_LITERALS =256;
    var zip_END_BLOCK = 256;
    var zip_L_CODES = zip_LITERALS + 1 + zip_LENGTH_CODES;
    var zip_D_CODES = 30;
    var zip_BL_CODES = 19;
    var zip_REP_3_6 = 16;
    var zip_REPZ_3_10 = 17;
    var zip_REPZ_11_138 = 18;
    var zip_HEAP_SIZE = 2 * zip_L_CODES + 1;
    var zip_H_SHIFT = parseInt((zip_HASH_BITS + zip_MIN_MATCH - 1) /
            zip_MIN_MATCH);

    /* variables */
    var zip_free_queue;
    var zip_qhead, zip_qtail;
    var zip_initflag;
    var zip_outbuf = null;
    var zip_outcnt, zip_outoff;
    var zip_complete;
    var zip_window;
    var zip_d_buf;
    var zip_l_buf;
    var zip_prev;
    var zip_bi_buf;
    var zip_bi_valid;
    var zip_block_start;
    var zip_ins_h;
    var zip_hash_head;
    var zip_prev_match;
    var zip_match_available;
    var zip_match_length;
    var zip_prev_length;
    var zip_strstart;
    var zip_match_start;
    var zip_eofile;
    var zip_lookahead;
    var zip_max_chain_length;
    var zip_max_lazy_match;
    var zip_compr_level;
    var zip_good_match;
    var zip_nice_match;
    var zip_dyn_ltree;
    var zip_dyn_dtree;
    var zip_static_ltree;
    var zip_static_dtree;
    var zip_bl_tree;
    var zip_l_desc;
    var zip_d_desc;
    var zip_bl_desc;
    var zip_bl_count;
    var zip_heap;
    var zip_heap_len;
    var zip_heap_max;
    var zip_depth;
    var zip_length_code;
    var zip_dist_code;
    var zip_base_length;
    var zip_base_dist;
    var zip_flag_buf;
    var zip_last_lit;
    var zip_last_dist;
    var zip_last_flags;
    var zip_flags;
    var zip_flag_bit;
    var zip_opt_len;
    var zip_static_len;
    var zip_deflate_data;
    var zip_deflate_pos;

    /* objects (deflate) */

    var zip_DeflateCT = function() {
        this.fc = 0; // frequency count or bit string
        this.dl = 0; // father node in Huffman tree or length of bit string
    }

    var zip_DeflateTreeDesc = function() {
        this.dyn_tree = null;	// the dynamic tree
        this.static_tree = null;	// corresponding static tree or NULL
        this.extra_bits = null;	// extra bits for each code or NULL
        this.extra_base = 0;	// base index for extra_bits
        this.elems = 0;		// max number of elements in the tree
        this.max_length = 0;	// max bit length for the codes
        this.max_code = 0;		// largest code with non zero frequency
    }

    /* Values for max_lazy_match, good_match and max_chain_length, depending on
     * the desired pack level (0..9). The values given below have been tuned to
     * exclude worst case performance for pathological files. Better values may be
     * found for specific files.
     */
    var zip_DeflateConfiguration = function(a, b, c, d) {
        this.good_length = a; // reduce lazy search above this match length
        this.max_lazy = b;    // do not perform lazy search above this match length
        this.nice_length = c; // quit search above this match length
        this.max_chain = d;
    }

    var zip_DeflateBuffer = function() {
        this.next = null;
        this.len = 0;
        this.ptr = new Array(zip_OUTBUFSIZ);
        this.off = 0;
    }

    /* constant tables */
    var zip_extra_lbits = new Array(
            0,0,0,0,0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5,0);
    var zip_extra_dbits = new Array(
            0,0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13);
    var zip_extra_blbits = new Array(
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,3,7);
    var zip_bl_order = new Array(
            16,17,18,0,8,7,9,6,10,5,11,4,12,3,13,2,14,1,15);
    var zip_configuration_table = new Array(
            new zip_DeflateConfiguration(0,    0,   0,    0),
            new zip_DeflateConfiguration(4,    4,   8,    4),
            new zip_DeflateConfiguration(4,    5,  16,    8),
            new zip_DeflateConfiguration(4,    6,  32,   32),
            new zip_DeflateConfiguration(4,    4,  16,   16),
            new zip_DeflateConfiguration(8,   16,  32,   32),
            new zip_DeflateConfiguration(8,   16, 128,  128),
            new zip_DeflateConfiguration(8,   32, 128,  256),
            new zip_DeflateConfiguration(32, 128, 258, 1024),
            new zip_DeflateConfiguration(32, 258, 258, 4096));


    /* routines (deflate) */

    var zip_deflate_start = function(level) {
        var i;

        if(!level)
            level = zip_DEFAULT_LEVEL;
        else if(level < 1)
            level = 1;
        else if(level > 9)
            level = 9;

        zip_compr_level = level;
        zip_initflag = false;
        zip_eofile = false;
        if(zip_outbuf != null)
            return;

        zip_free_queue = zip_qhead = zip_qtail = null;
        zip_outbuf = new Array(zip_OUTBUFSIZ);
        zip_window = new Array(zip_window_size);
        zip_d_buf = new Array(zip_DIST_BUFSIZE);
        zip_l_buf = new Array(zip_INBUFSIZ + zip_INBUF_EXTRA);
        zip_prev = new Array(1 << zip_BITS);
        zip_dyn_ltree = new Array(zip_HEAP_SIZE);
        for(i = 0; i < zip_HEAP_SIZE; i++)
            zip_dyn_ltree[i] = new zip_DeflateCT();
        zip_dyn_dtree = new Array(2*zip_D_CODES+1);
        for(i = 0; i < 2*zip_D_CODES+1; i++)
            zip_dyn_dtree[i] = new zip_DeflateCT();
        zip_static_ltree = new Array(zip_L_CODES+2);
        for(i = 0; i < zip_L_CODES+2; i++)
            zip_static_ltree[i] = new zip_DeflateCT();
        zip_static_dtree = new Array(zip_D_CODES);
        for(i = 0; i < zip_D_CODES; i++)
            zip_static_dtree[i] = new zip_DeflateCT();
        zip_bl_tree = new Array(2*zip_BL_CODES+1);
        for(i = 0; i < 2*zip_BL_CODES+1; i++)
            zip_bl_tree[i] = new zip_DeflateCT();
        zip_l_desc = new zip_DeflateTreeDesc();
        zip_d_desc = new zip_DeflateTreeDesc();
        zip_bl_desc = new zip_DeflateTreeDesc();
        zip_bl_count = new Array(zip_MAX_BITS+1);
        zip_heap = new Array(2*zip_L_CODES+1);
        zip_depth = new Array(2*zip_L_CODES+1);
        zip_length_code = new Array(zip_MAX_MATCH-zip_MIN_MATCH+1);
        zip_dist_code = new Array(512);
        zip_base_length = new Array(zip_LENGTH_CODES);
        zip_base_dist = new Array(zip_D_CODES);
        zip_flag_buf = new Array(parseInt(zip_LIT_BUFSIZE / 8));
    }

    var zip_deflate_end = function() {
        zip_free_queue = zip_qhead = zip_qtail = null;
        zip_outbuf = null;
        zip_window = null;
        zip_d_buf = null;
        zip_l_buf = null;
        zip_prev = null;
        zip_dyn_ltree = null;
        zip_dyn_dtree = null;
        zip_static_ltree = null;
        zip_static_dtree = null;
        zip_bl_tree = null;
        zip_l_desc = null;
        zip_d_desc = null;
        zip_bl_desc = null;
        zip_bl_count = null;
        zip_heap = null;
        zip_depth = null;
        zip_length_code = null;
        zip_dist_code = null;
        zip_base_length = null;
        zip_base_dist = null;
        zip_flag_buf = null;
    }

    var zip_reuse_queue = function(p) {
        p.next = zip_free_queue;
        zip_free_queue = p;
    }

    var zip_new_queue = function() {
        var p;

        if(zip_free_queue != null)
        {
            p = zip_free_queue;
            zip_free_queue = zip_free_queue.next;
        }
        else
            p = new zip_DeflateBuffer();
        p.next = null;
        p.len = p.off = 0;

        return p;
    }

    var zip_head1 = function(i) {
        return zip_prev[zip_WSIZE + i];
    }

    var zip_head2 = function(i, val) {
        return zip_prev[zip_WSIZE + i] = val;
    }

    /* put_byte is used for the compressed output, put_ubyte for the
     * uncompressed output. However unlzw() uses window for its
     * suffix table instead of its output buffer, so it does not use put_ubyte
     * (to be cleaned up).
     */
    var zip_put_byte = function(c) {
        zip_outbuf[zip_outoff + zip_outcnt++] = c;
        if(zip_outoff + zip_outcnt == zip_OUTBUFSIZ)
            zip_qoutbuf();
    }

    /* Output a 16 bit value, lsb first */
    var zip_put_short = function(w) {
        w &= 0xffff;
        if(zip_outoff + zip_outcnt < zip_OUTBUFSIZ - 2) {
            zip_outbuf[zip_outoff + zip_outcnt++] = (w & 0xff);
            zip_outbuf[zip_outoff + zip_outcnt++] = (w >>> 8);
        } else {
            zip_put_byte(w & 0xff);
            zip_put_byte(w >>> 8);
        }
    }

    /* ==========================================================================
     * Insert string s in the dictionary and set match_head to the previous head
     * of the hash chain (the most recent string with same hash key). Return
     * the previous length of the hash chain.
     * IN  assertion: all calls to to INSERT_STRING are made with consecutive
     *    input characters and the first MIN_MATCH bytes of s are valid
     *    (except for the last MIN_MATCH-1 bytes of the input file).
     */
    var zip_INSERT_STRING = function() {
        zip_ins_h = ((zip_ins_h << zip_H_SHIFT)
                ^ (zip_window[zip_strstart + zip_MIN_MATCH - 1] & 0xff))
                & zip_HASH_MASK;
        zip_hash_head = zip_head1(zip_ins_h);
        zip_prev[zip_strstart & zip_WMASK] = zip_hash_head;
        zip_head2(zip_ins_h, zip_strstart);
    }

    /* Send a code of the given tree. c and tree must not have side effects */
    var zip_SEND_CODE = function(c, tree) {
        zip_send_bits(tree[c].fc, tree[c].dl);
    }

    /* Mapping from a distance to a distance code. dist is the distance - 1 and
     * must not have side effects. dist_code[256] and dist_code[257] are never
     * used.
     */
    var zip_D_CODE = function(dist) {
        return (dist < 256 ? zip_dist_code[dist]
                : zip_dist_code[256 + (dist>>7)]) & 0xff;
    }

    /* ==========================================================================
     * Compares to subtrees, using the tree depth as tie breaker when
     * the subtrees have equal frequency. This minimizes the worst case length.
     */
    var zip_SMALLER = function(tree, n, m) {
        return tree[n].fc < tree[m].fc ||
                (tree[n].fc == tree[m].fc && zip_depth[n] <= zip_depth[m]);
    }

    /* ==========================================================================
     * read string data
     */
    var zip_read_buff = function(buff, offset, n) {
        var i;
        for(i = 0; i < n && zip_deflate_pos < zip_deflate_data.length; i++)
            buff[offset + i] =
                    zip_deflate_data.charCodeAt(zip_deflate_pos++) & 0xff;
        return i;
    }

    /* ==========================================================================
     * Initialize the "longest match" routines for a new file
     */
    var zip_lm_init = function() {
        var j;

        /* Initialize the hash table. */
        for(j = 0; j < zip_HASH_SIZE; j++)
//	zip_head2(j, zip_NIL);
            zip_prev[zip_WSIZE + j] = 0;
        /* prev will be initialized on the fly */

        /* Set the default configuration parameters:
         */
        zip_max_lazy_match = zip_configuration_table[zip_compr_level].max_lazy;
        zip_good_match     = zip_configuration_table[zip_compr_level].good_length;
        if(!zip_FULL_SEARCH)
            zip_nice_match = zip_configuration_table[zip_compr_level].nice_length;
        zip_max_chain_length = zip_configuration_table[zip_compr_level].max_chain;

        zip_strstart = 0;
        zip_block_start = 0;

        zip_lookahead = zip_read_buff(zip_window, 0, 2 * zip_WSIZE);
        if(zip_lookahead <= 0) {
            zip_eofile = true;
            zip_lookahead = 0;
            return;
        }
        zip_eofile = false;
        /* Make sure that we always have enough lookahead. This is important
         * if input comes from a device such as a tty.
         */
        while(zip_lookahead < zip_MIN_LOOKAHEAD && !zip_eofile)
            zip_fill_window();

        /* If lookahead < MIN_MATCH, ins_h is garbage, but this is
         * not important since only literal bytes will be emitted.
         */
        zip_ins_h = 0;
        for(j = 0; j < zip_MIN_MATCH - 1; j++) {
//      UPDATE_HASH(ins_h, window[j]);
            zip_ins_h = ((zip_ins_h << zip_H_SHIFT) ^ (zip_window[j] & 0xff)) & zip_HASH_MASK;
        }
    }

    /* ==========================================================================
     * Set match_start to the longest match starting at the given string and
     * return its length. Matches shorter or equal to prev_length are discarded,
     * in which case the result is equal to prev_length and match_start is
     * garbage.
     * IN assertions: cur_match is the head of the hash chain for the current
     *   string (strstart) and its distance is <= MAX_DIST, and prev_length >= 1
     */
    var zip_longest_match = function(cur_match) {
        var chain_length = zip_max_chain_length; // max hash chain length
        var scanp = zip_strstart; // current string
        var matchp;		// matched string
        var len;		// length of current match
        var best_len = zip_prev_length;	// best match length so far

        /* Stop when cur_match becomes <= limit. To simplify the code,
         * we prevent matches with the string of window index 0.
         */
        var limit = (zip_strstart > zip_MAX_DIST ? zip_strstart - zip_MAX_DIST : zip_NIL);

        var strendp = zip_strstart + zip_MAX_MATCH;
        var scan_end1 = zip_window[scanp + best_len - 1];
        var scan_end  = zip_window[scanp + best_len];

        /* Do not waste too much time if we already have a good match: */
        if(zip_prev_length >= zip_good_match)
            chain_length >>= 2;

//  Assert(encoder->strstart <= window_size-MIN_LOOKAHEAD, "insufficient lookahead");

        do {
//    Assert(cur_match < encoder->strstart, "no future");
            matchp = cur_match;

            /* Skip to next match if the match length cannot increase
             * or if the match length is less than 2:
             */
            if(zip_window[matchp + best_len]	!= scan_end  ||
                    zip_window[matchp + best_len - 1]	!= scan_end1 ||
                    zip_window[matchp]			!= zip_window[scanp] ||
                    zip_window[++matchp]			!= zip_window[scanp + 1]) {
                continue;
            }

            /* The check at best_len-1 can be removed because it will be made
             * again later. (This heuristic is not always a win.)
             * It is not necessary to compare scan[2] and match[2] since they
             * are always equal when the other bytes match, given that
             * the hash keys are equal and that HASH_BITS >= 8.
             */
            scanp += 2;
            matchp++;

            /* We check for insufficient lookahead only every 8th comparison;
             * the 256th check will be made at strstart+258.
             */
            do {
            } while(zip_window[++scanp] == zip_window[++matchp] &&
                    zip_window[++scanp] == zip_window[++matchp] &&
                    zip_window[++scanp] == zip_window[++matchp] &&
                    zip_window[++scanp] == zip_window[++matchp] &&
                    zip_window[++scanp] == zip_window[++matchp] &&
                    zip_window[++scanp] == zip_window[++matchp] &&
                    zip_window[++scanp] == zip_window[++matchp] &&
                    zip_window[++scanp] == zip_window[++matchp] &&
                    scanp < strendp);

            len = zip_MAX_MATCH - (strendp - scanp);
            scanp = strendp - zip_MAX_MATCH;

            if(len > best_len) {
                zip_match_start = cur_match;
                best_len = len;
                if(zip_FULL_SEARCH) {
                    if(len >= zip_MAX_MATCH) break;
                } else {
                    if(len >= zip_nice_match) break;
                }

                scan_end1  = zip_window[scanp + best_len-1];
                scan_end   = zip_window[scanp + best_len];
            }
        } while((cur_match = zip_prev[cur_match & zip_WMASK]) > limit
                && --chain_length != 0);

        return best_len;
    }

    /* ==========================================================================
     * Fill the window when the lookahead becomes insufficient.
     * Updates strstart and lookahead, and sets eofile if end of input file.
     * IN assertion: lookahead < MIN_LOOKAHEAD && strstart + lookahead > 0
     * OUT assertions: at least one byte has been read, or eofile is set;
     *    file reads are performed for at least two bytes (required for the
     *    translate_eol option).
     */
    var zip_fill_window = function() {
        var n, m;

        // Amount of free space at the end of the window.
        var more = zip_window_size - zip_lookahead - zip_strstart;

        /* If the window is almost full and there is insufficient lookahead,
         * move the upper half to the lower one to make room in the upper half.
         */
        if(more == -1) {
            /* Very unlikely, but possible on 16 bit machine if strstart == 0
             * and lookahead == 1 (input done one byte at time)
             */
            more--;
        } else if(zip_strstart >= zip_WSIZE + zip_MAX_DIST) {
            /* By the IN assertion, the window is not empty so we can't confuse
             * more == 0 with more == 64K on a 16 bit machine.
             */
//	Assert(window_size == (ulg)2*WSIZE, "no sliding with BIG_MEM");

//	System.arraycopy(window, WSIZE, window, 0, WSIZE);
            for(n = 0; n < zip_WSIZE; n++)
                zip_window[n] = zip_window[n + zip_WSIZE];

            zip_match_start -= zip_WSIZE;
            zip_strstart    -= zip_WSIZE; /* we now have strstart >= MAX_DIST: */
            zip_block_start -= zip_WSIZE;

            for(n = 0; n < zip_HASH_SIZE; n++) {
                m = zip_head1(n);
                zip_head2(n, m >= zip_WSIZE ? m - zip_WSIZE : zip_NIL);
            }
            for(n = 0; n < zip_WSIZE; n++) {
                /* If n is not on any hash chain, prev[n] is garbage but
                 * its value will never be used.
                 */
                m = zip_prev[n];
                zip_prev[n] = (m >= zip_WSIZE ? m - zip_WSIZE : zip_NIL);
            }
            more += zip_WSIZE;
        }
        // At this point, more >= 2
        if(!zip_eofile) {
            n = zip_read_buff(zip_window, zip_strstart + zip_lookahead, more);
            if(n <= 0)
                zip_eofile = true;
            else
                zip_lookahead += n;
        }
    }

    /* ==========================================================================
     * Processes a new input file and return its compressed length. This
     * function does not perform lazy evaluationof matches and inserts
     * new strings in the dictionary only for unmatched strings or for short
     * matches. It is used only for the fast compression options.
     */
    var zip_deflate_fast = function() {
        while(zip_lookahead != 0 && zip_qhead == null) {
            var flush; // set if current block must be flushed

            /* Insert the string window[strstart .. strstart+2] in the
             * dictionary, and set hash_head to the head of the hash chain:
             */
            zip_INSERT_STRING();

            /* Find the longest match, discarding those <= prev_length.
             * At this point we have always match_length < MIN_MATCH
             */
            if(zip_hash_head != zip_NIL &&
                    zip_strstart - zip_hash_head <= zip_MAX_DIST) {
                /* To simplify the code, we prevent matches with the string
                 * of window index 0 (in particular we have to avoid a match
                 * of the string with itself at the start of the input file).
                 */
                zip_match_length = zip_longest_match(zip_hash_head);
                /* longest_match() sets match_start */
                if(zip_match_length > zip_lookahead)
                    zip_match_length = zip_lookahead;
            }
            if(zip_match_length >= zip_MIN_MATCH) {
//	    check_match(strstart, match_start, match_length);

                flush = zip_ct_tally(zip_strstart - zip_match_start,
                        zip_match_length - zip_MIN_MATCH);
                zip_lookahead -= zip_match_length;

                /* Insert new strings in the hash table only if the match length
                 * is not too large. This saves time but degrades compression.
                 */
                if(zip_match_length <= zip_max_lazy_match) {
                    zip_match_length--; // string at strstart already in hash table
                    do {
                        zip_strstart++;
                        zip_INSERT_STRING();
                        /* strstart never exceeds WSIZE-MAX_MATCH, so there are
                         * always MIN_MATCH bytes ahead. If lookahead < MIN_MATCH
                         * these bytes are garbage, but it does not matter since
                         * the next lookahead bytes will be emitted as literals.
                         */
                    } while(--zip_match_length != 0);
                    zip_strstart++;
                } else {
                    zip_strstart += zip_match_length;
                    zip_match_length = 0;
                    zip_ins_h = zip_window[zip_strstart] & 0xff;
//		UPDATE_HASH(ins_h, window[strstart + 1]);
                    zip_ins_h = ((zip_ins_h<<zip_H_SHIFT) ^ (zip_window[zip_strstart + 1] & 0xff)) & zip_HASH_MASK;

//#if MIN_MATCH != 3
//		Call UPDATE_HASH() MIN_MATCH-3 more times
//#endif

                }
            } else {
                /* No match, output a literal byte */
                flush = zip_ct_tally(0, zip_window[zip_strstart] & 0xff);
                zip_lookahead--;
                zip_strstart++;
            }
            if(flush) {
                zip_flush_block(0);
                zip_block_start = zip_strstart;
            }

            /* Make sure that we always have enough lookahead, except
             * at the end of the input file. We need MAX_MATCH bytes
             * for the next match, plus MIN_MATCH bytes to insert the
             * string following the next match.
             */
            while(zip_lookahead < zip_MIN_LOOKAHEAD && !zip_eofile)
                zip_fill_window();
        }
    }

    var zip_deflate_better = function() {
        /* Process the input block. */
        while(zip_lookahead != 0 && zip_qhead == null) {
            /* Insert the string window[strstart .. strstart+2] in the
             * dictionary, and set hash_head to the head of the hash chain:
             */
            zip_INSERT_STRING();

            /* Find the longest match, discarding those <= prev_length.
             */
            zip_prev_length = zip_match_length;
            zip_prev_match = zip_match_start;
            zip_match_length = zip_MIN_MATCH - 1;

            if(zip_hash_head != zip_NIL &&
                    zip_prev_length < zip_max_lazy_match &&
                    zip_strstart - zip_hash_head <= zip_MAX_DIST) {
                /* To simplify the code, we prevent matches with the string
                 * of window index 0 (in particular we have to avoid a match
                 * of the string with itself at the start of the input file).
                 */
                zip_match_length = zip_longest_match(zip_hash_head);
                /* longest_match() sets match_start */
                if(zip_match_length > zip_lookahead)
                    zip_match_length = zip_lookahead;

                /* Ignore a length 3 match if it is too distant: */
                if(zip_match_length == zip_MIN_MATCH &&
                        zip_strstart - zip_match_start > zip_TOO_FAR) {
                    /* If prev_match is also MIN_MATCH, match_start is garbage
                     * but we will ignore the current match anyway.
                     */
                    zip_match_length--;
                }
            }
            /* If there was a match at the previous step and the current
             * match is not better, output the previous match:
             */
            if(zip_prev_length >= zip_MIN_MATCH &&
                    zip_match_length <= zip_prev_length) {
                var flush; // set if current block must be flushed

//	    check_match(strstart - 1, prev_match, prev_length);
                flush = zip_ct_tally(zip_strstart - 1 - zip_prev_match,
                        zip_prev_length - zip_MIN_MATCH);

                /* Insert in hash table all strings up to the end of the match.
                 * strstart-1 and strstart are already inserted.
                 */
                zip_lookahead -= zip_prev_length - 1;
                zip_prev_length -= 2;
                do {
                    zip_strstart++;
                    zip_INSERT_STRING();
                    /* strstart never exceeds WSIZE-MAX_MATCH, so there are
                     * always MIN_MATCH bytes ahead. If lookahead < MIN_MATCH
                     * these bytes are garbage, but it does not matter since the
                     * next lookahead bytes will always be emitted as literals.
                     */
                } while(--zip_prev_length != 0);
                zip_match_available = 0;
                zip_match_length = zip_MIN_MATCH - 1;
                zip_strstart++;
                if(flush) {
                    zip_flush_block(0);
                    zip_block_start = zip_strstart;
                }
            } else if(zip_match_available != 0) {
                /* If there was no match at the previous position, output a
                 * single literal. If there was a match but the current match
                 * is longer, truncate the previous match to a single literal.
                 */
                if(zip_ct_tally(0, zip_window[zip_strstart - 1] & 0xff)) {
                    zip_flush_block(0);
                    zip_block_start = zip_strstart;
                }
                zip_strstart++;
                zip_lookahead--;
            } else {
                /* There is no previous match to compare with, wait for
                 * the next step to decide.
                 */
                zip_match_available = 1;
                zip_strstart++;
                zip_lookahead--;
            }

            /* Make sure that we always have enough lookahead, except
             * at the end of the input file. We need MAX_MATCH bytes
             * for the next match, plus MIN_MATCH bytes to insert the
             * string following the next match.
             */
            while(zip_lookahead < zip_MIN_LOOKAHEAD && !zip_eofile)
                zip_fill_window();
        }
    }

    var zip_init_deflate = function() {
        if(zip_eofile)
            return;
        zip_bi_buf = 0;
        zip_bi_valid = 0;
        zip_ct_init();
        zip_lm_init();

        zip_qhead = null;
        zip_outcnt = 0;
        zip_outoff = 0;

        if(zip_compr_level <= 3)
        {
            zip_prev_length = zip_MIN_MATCH - 1;
            zip_match_length = 0;
        }
        else
        {
            zip_match_length = zip_MIN_MATCH - 1;
            zip_match_available = 0;
        }

        zip_complete = false;
    }

    /* ==========================================================================
     * Same as above, but achieves better compression. We use a lazy
     * evaluation for matches: a match is finally adopted only if there is
     * no better match at the next window position.
     */
    var zip_deflate_internal = function(buff, off, buff_size) {
        var n;

        if(!zip_initflag)
        {
            zip_init_deflate();
            zip_initflag = true;
            if(zip_lookahead == 0) { // empty
                zip_complete = true;
                return 0;
            }
        }

        if((n = zip_qcopy(buff, off, buff_size)) == buff_size)
            return buff_size;

        if(zip_complete)
            return n;

        if(zip_compr_level <= 3) // optimized for speed
            zip_deflate_fast();
        else
            zip_deflate_better();
        if(zip_lookahead == 0) {
            if(zip_match_available != 0)
                zip_ct_tally(0, zip_window[zip_strstart - 1] & 0xff);
            zip_flush_block(1);
            zip_complete = true;
        }
        return n + zip_qcopy(buff, n + off, buff_size - n);
    }

    var zip_qcopy = function(buff, off, buff_size) {
        var n, i, j;

        n = 0;
        while(zip_qhead != null && n < buff_size)
        {
            i = buff_size - n;
            if(i > zip_qhead.len)
                i = zip_qhead.len;
//      System.arraycopy(qhead.ptr, qhead.off, buff, off + n, i);
            for(j = 0; j < i; j++)
                buff[off + n + j] = zip_qhead.ptr[zip_qhead.off + j];

            zip_qhead.off += i;
            zip_qhead.len -= i;
            n += i;
            if(zip_qhead.len == 0) {
                var p;
                p = zip_qhead;
                zip_qhead = zip_qhead.next;
                zip_reuse_queue(p);
            }
        }

        if(n == buff_size)
            return n;

        if(zip_outoff < zip_outcnt) {
            i = buff_size - n;
            if(i > zip_outcnt - zip_outoff)
                i = zip_outcnt - zip_outoff;
            // System.arraycopy(outbuf, outoff, buff, off + n, i);
            for(j = 0; j < i; j++)
                buff[off + n + j] = zip_outbuf[zip_outoff + j];
            zip_outoff += i;
            n += i;
            if(zip_outcnt == zip_outoff)
                zip_outcnt = zip_outoff = 0;
        }
        return n;
    }

    /* ==========================================================================
     * Allocate the match buffer, initialize the various tables and save the
     * location of the internal file attribute (ascii/binary) and method
     * (DEFLATE/STORE).
     */
    var zip_ct_init = function() {
        var n;	// iterates over tree elements
        var bits;	// bit counter
        var length;	// length value
        var code;	// code value
        var dist;	// distance index

        if(zip_static_dtree[0].dl != 0) return; // ct_init already called

        zip_l_desc.dyn_tree		= zip_dyn_ltree;
        zip_l_desc.static_tree	= zip_static_ltree;
        zip_l_desc.extra_bits	= zip_extra_lbits;
        zip_l_desc.extra_base	= zip_LITERALS + 1;
        zip_l_desc.elems		= zip_L_CODES;
        zip_l_desc.max_length	= zip_MAX_BITS;
        zip_l_desc.max_code		= 0;

        zip_d_desc.dyn_tree		= zip_dyn_dtree;
        zip_d_desc.static_tree	= zip_static_dtree;
        zip_d_desc.extra_bits	= zip_extra_dbits;
        zip_d_desc.extra_base	= 0;
        zip_d_desc.elems		= zip_D_CODES;
        zip_d_desc.max_length	= zip_MAX_BITS;
        zip_d_desc.max_code		= 0;

        zip_bl_desc.dyn_tree	= zip_bl_tree;
        zip_bl_desc.static_tree	= null;
        zip_bl_desc.extra_bits	= zip_extra_blbits;
        zip_bl_desc.extra_base	= 0;
        zip_bl_desc.elems		= zip_BL_CODES;
        zip_bl_desc.max_length	= zip_MAX_BL_BITS;
        zip_bl_desc.max_code	= 0;

        // Initialize the mapping length (0..255) -> length code (0..28)
        length = 0;
        for(code = 0; code < zip_LENGTH_CODES-1; code++) {
            zip_base_length[code] = length;
            for(n = 0; n < (1<<zip_extra_lbits[code]); n++)
                zip_length_code[length++] = code;
        }
        // Assert (length == 256, "ct_init: length != 256");

        /* Note that the length 255 (match length 258) can be represented
         * in two different ways: code 284 + 5 bits or code 285, so we
         * overwrite length_code[255] to use the best encoding:
         */
        zip_length_code[length-1] = code;

        /* Initialize the mapping dist (0..32K) -> dist code (0..29) */
        dist = 0;
        for(code = 0 ; code < 16; code++) {
            zip_base_dist[code] = dist;
            for(n = 0; n < (1<<zip_extra_dbits[code]); n++) {
                zip_dist_code[dist++] = code;
            }
        }
        // Assert (dist == 256, "ct_init: dist != 256");
        dist >>= 7; // from now on, all distances are divided by 128
        for( ; code < zip_D_CODES; code++) {
            zip_base_dist[code] = dist << 7;
            for(n = 0; n < (1<<(zip_extra_dbits[code]-7)); n++)
                zip_dist_code[256 + dist++] = code;
        }
        // Assert (dist == 256, "ct_init: 256+dist != 512");

        // Construct the codes of the static literal tree
        for(bits = 0; bits <= zip_MAX_BITS; bits++)
            zip_bl_count[bits] = 0;
        n = 0;
        while(n <= 143) { zip_static_ltree[n++].dl = 8; zip_bl_count[8]++; }
        while(n <= 255) { zip_static_ltree[n++].dl = 9; zip_bl_count[9]++; }
        while(n <= 279) { zip_static_ltree[n++].dl = 7; zip_bl_count[7]++; }
        while(n <= 287) { zip_static_ltree[n++].dl = 8; zip_bl_count[8]++; }
        /* Codes 286 and 287 do not exist, but we must include them in the
         * tree construction to get a canonical Huffman tree (longest code
         * all ones)
         */
        zip_gen_codes(zip_static_ltree, zip_L_CODES + 1);

        /* The static distance tree is trivial: */
        for(n = 0; n < zip_D_CODES; n++) {
            zip_static_dtree[n].dl = 5;
            zip_static_dtree[n].fc = zip_bi_reverse(n, 5);
        }

        // Initialize the first block of the first file:
        zip_init_block();
    }

    /* ==========================================================================
     * Initialize a new block.
     */
    var zip_init_block = function() {
        var n; // iterates over tree elements

        // Initialize the trees.
        for(n = 0; n < zip_L_CODES;  n++) zip_dyn_ltree[n].fc = 0;
        for(n = 0; n < zip_D_CODES;  n++) zip_dyn_dtree[n].fc = 0;
        for(n = 0; n < zip_BL_CODES; n++) zip_bl_tree[n].fc = 0;

        zip_dyn_ltree[zip_END_BLOCK].fc = 1;
        zip_opt_len = zip_static_len = 0;
        zip_last_lit = zip_last_dist = zip_last_flags = 0;
        zip_flags = 0;
        zip_flag_bit = 1;
    }

    /* ==========================================================================
     * Restore the heap property by moving down the tree starting at node k,
     * exchanging a node with the smallest of its two sons if necessary, stopping
     * when the heap property is re-established (each father smaller than its
     * two sons).
     */
    var zip_pqdownheap = function(
            tree,	// the tree to restore
            k) {	// node to move down
        var v = zip_heap[k];
        var j = k << 1;	// left son of k

        while(j <= zip_heap_len) {
            // Set j to the smallest of the two sons:
            if(j < zip_heap_len &&
                    zip_SMALLER(tree, zip_heap[j + 1], zip_heap[j]))
                j++;

            // Exit if v is smaller than both sons
            if(zip_SMALLER(tree, v, zip_heap[j]))
                break;

            // Exchange v with the smallest son
            zip_heap[k] = zip_heap[j];
            k = j;

            // And continue down the tree, setting j to the left son of k
            j <<= 1;
        }
        zip_heap[k] = v;
    }

    /* ==========================================================================
     * Compute the optimal bit lengths for a tree and update the total bit length
     * for the current block.
     * IN assertion: the fields freq and dad are set, heap[heap_max] and
     *    above are the tree nodes sorted by increasing frequency.
     * OUT assertions: the field len is set to the optimal bit length, the
     *     array bl_count contains the frequencies for each bit length.
     *     The length opt_len is updated; static_len is also updated if stree is
     *     not null.
     */
    var zip_gen_bitlen = function(desc) { // the tree descriptor
        var tree		= desc.dyn_tree;
        var extra		= desc.extra_bits;
        var base		= desc.extra_base;
        var max_code	= desc.max_code;
        var max_length	= desc.max_length;
        var stree		= desc.static_tree;
        var h;		// heap index
        var n, m;		// iterate over the tree elements
        var bits;		// bit length
        var xbits;		// extra bits
        var f;		// frequency
        var overflow = 0;	// number of elements with bit length too large

        for(bits = 0; bits <= zip_MAX_BITS; bits++)
            zip_bl_count[bits] = 0;

        /* In a first pass, compute the optimal bit lengths (which may
         * overflow in the case of the bit length tree).
         */
        tree[zip_heap[zip_heap_max]].dl = 0; // root of the heap

        for(h = zip_heap_max + 1; h < zip_HEAP_SIZE; h++) {
            n = zip_heap[h];
            bits = tree[tree[n].dl].dl + 1;
            if(bits > max_length) {
                bits = max_length;
                overflow++;
            }
            tree[n].dl = bits;
            // We overwrite tree[n].dl which is no longer needed

            if(n > max_code)
                continue; // not a leaf node

            zip_bl_count[bits]++;
            xbits = 0;
            if(n >= base)
                xbits = extra[n - base];
            f = tree[n].fc;
            zip_opt_len += f * (bits + xbits);
            if(stree != null)
                zip_static_len += f * (stree[n].dl + xbits);
        }
        if(overflow == 0)
            return;

        // This happens for example on obj2 and pic of the Calgary corpus

        // Find the first bit length which could increase:
        do {
            bits = max_length - 1;
            while(zip_bl_count[bits] == 0)
                bits--;
            zip_bl_count[bits]--;		// move one leaf down the tree
            zip_bl_count[bits + 1] += 2;	// move one overflow item as its brother
            zip_bl_count[max_length]--;
            /* The brother of the overflow item also moves one step up,
             * but this does not affect bl_count[max_length]
             */
            overflow -= 2;
        } while(overflow > 0);

        /* Now recompute all bit lengths, scanning in increasing frequency.
         * h is still equal to HEAP_SIZE. (It is simpler to reconstruct all
         * lengths instead of fixing only the wrong ones. This idea is taken
         * from 'ar' written by Haruhiko Okumura.)
         */
        for(bits = max_length; bits != 0; bits--) {
            n = zip_bl_count[bits];
            while(n != 0) {
                m = zip_heap[--h];
                if(m > max_code)
                    continue;
                if(tree[m].dl != bits) {
                    zip_opt_len += (bits - tree[m].dl) * tree[m].fc;
                    tree[m].fc = bits;
                }
                n--;
            }
        }
    }

    /* ==========================================================================
     * Generate the codes for a given tree and bit counts (which need not be
     * optimal).
     * IN assertion: the array bl_count contains the bit length statistics for
     * the given tree and the field len is set for all tree elements.
     * OUT assertion: the field code is set for all tree elements of non
     *     zero code length.
     */
    var zip_gen_codes = function(tree,	// the tree to decorate
                                 max_code) {	// largest code with non zero frequency
        var next_code = new Array(zip_MAX_BITS+1); // next code value for each bit length
        var code = 0;		// running code value
        var bits;			// bit index
        var n;			// code index

        /* The distribution counts are first used to generate the code values
         * without bit reversal.
         */
        for(bits = 1; bits <= zip_MAX_BITS; bits++) {
            code = ((code + zip_bl_count[bits-1]) << 1);
            next_code[bits] = code;
        }

        /* Check that the bit counts in bl_count are consistent. The last code
         * must be all ones.
         */
//    Assert (code + encoder->bl_count[MAX_BITS]-1 == (1<<MAX_BITS)-1,
//	    "inconsistent bit counts");
//    Tracev((stderr,"\ngen_codes: max_code %d ", max_code));

        for(n = 0; n <= max_code; n++) {
            var len = tree[n].dl;
            if(len == 0)
                continue;
            // Now reverse the bits
            tree[n].fc = zip_bi_reverse(next_code[len]++, len);

//      Tracec(tree != static_ltree, (stderr,"\nn %3d %c l %2d c %4x (%x) ",
//	  n, (isgraph(n) ? n : ' '), len, tree[n].fc, next_code[len]-1));
        }
    }

    /* ==========================================================================
     * Construct one Huffman tree and assigns the code bit strings and lengths.
     * Update the total bit length for the current block.
     * IN assertion: the field freq is set for all tree elements.
     * OUT assertions: the fields len and code are set to the optimal bit length
     *     and corresponding code. The length opt_len is updated; static_len is
     *     also updated if stree is not null. The field max_code is set.
     */
    var zip_build_tree = function(desc) { // the tree descriptor
        var tree	= desc.dyn_tree;
        var stree	= desc.static_tree;
        var elems	= desc.elems;
        var n, m;		// iterate over heap elements
        var max_code = -1;	// largest code with non zero frequency
        var node = elems;	// next internal node of the tree

        /* Construct the initial heap, with least frequent element in
         * heap[SMALLEST]. The sons of heap[n] are heap[2*n] and heap[2*n+1].
         * heap[0] is not used.
         */
        zip_heap_len = 0;
        zip_heap_max = zip_HEAP_SIZE;

        for(n = 0; n < elems; n++) {
            if(tree[n].fc != 0) {
                zip_heap[++zip_heap_len] = max_code = n;
                zip_depth[n] = 0;
            } else
                tree[n].dl = 0;
        }

        /* The pkzip format requires that at least one distance code exists,
         * and that at least one bit should be sent even if there is only one
         * possible code. So to avoid special checks later on we force at least
         * two codes of non zero frequency.
         */
        while(zip_heap_len < 2) {
            var xnew = zip_heap[++zip_heap_len] = (max_code < 2 ? ++max_code : 0);
            tree[xnew].fc = 1;
            zip_depth[xnew] = 0;
            zip_opt_len--;
            if(stree != null)
                zip_static_len -= stree[xnew].dl;
            // new is 0 or 1 so it does not have extra bits
        }
        desc.max_code = max_code;

        /* The elements heap[heap_len/2+1 .. heap_len] are leaves of the tree,
         * establish sub-heaps of increasing lengths:
         */
        for(n = zip_heap_len >> 1; n >= 1; n--)
            zip_pqdownheap(tree, n);

        /* Construct the Huffman tree by repeatedly combining the least two
         * frequent nodes.
         */
        do {
            n = zip_heap[zip_SMALLEST];
            zip_heap[zip_SMALLEST] = zip_heap[zip_heap_len--];
            zip_pqdownheap(tree, zip_SMALLEST);

            m = zip_heap[zip_SMALLEST];  // m = node of next least frequency

            // keep the nodes sorted by frequency
            zip_heap[--zip_heap_max] = n;
            zip_heap[--zip_heap_max] = m;

            // Create a new node father of n and m
            tree[node].fc = tree[n].fc + tree[m].fc;
//	depth[node] = (char)(MAX(depth[n], depth[m]) + 1);
            if(zip_depth[n] > zip_depth[m] + 1)
                zip_depth[node] = zip_depth[n];
            else
                zip_depth[node] = zip_depth[m] + 1;
            tree[n].dl = tree[m].dl = node;

            // and insert the new node in the heap
            zip_heap[zip_SMALLEST] = node++;
            zip_pqdownheap(tree, zip_SMALLEST);

        } while(zip_heap_len >= 2);

        zip_heap[--zip_heap_max] = zip_heap[zip_SMALLEST];

        /* At this point, the fields freq and dad are set. We can now
         * generate the bit lengths.
         */
        zip_gen_bitlen(desc);

        // The field len is now set, we can generate the bit codes
        zip_gen_codes(tree, max_code);
    }

    /* ==========================================================================
     * Scan a literal or distance tree to determine the frequencies of the codes
     * in the bit length tree. Updates opt_len to take into account the repeat
     * counts. (The contribution of the bit length codes will be added later
     * during the construction of bl_tree.)
     */
    var zip_scan_tree = function(tree,// the tree to be scanned
                                 max_code) {  // and its largest code of non zero frequency
        var n;			// iterates over all tree elements
        var prevlen = -1;		// last emitted length
        var curlen;			// length of current code
        var nextlen = tree[0].dl;	// length of next code
        var count = 0;		// repeat count of the current code
        var max_count = 7;		// max repeat count
        var min_count = 4;		// min repeat count

        if(nextlen == 0) {
            max_count = 138;
            min_count = 3;
        }
        tree[max_code + 1].dl = 0xffff; // guard

        for(n = 0; n <= max_code; n++) {
            curlen = nextlen;
            nextlen = tree[n + 1].dl;
            if(++count < max_count && curlen == nextlen)
                continue;
            else if(count < min_count)
                zip_bl_tree[curlen].fc += count;
            else if(curlen != 0) {
                if(curlen != prevlen)
                    zip_bl_tree[curlen].fc++;
                zip_bl_tree[zip_REP_3_6].fc++;
            } else if(count <= 10)
                zip_bl_tree[zip_REPZ_3_10].fc++;
            else
                zip_bl_tree[zip_REPZ_11_138].fc++;
            count = 0; prevlen = curlen;
            if(nextlen == 0) {
                max_count = 138;
                min_count = 3;
            } else if(curlen == nextlen) {
                max_count = 6;
                min_count = 3;
            } else {
                max_count = 7;
                min_count = 4;
            }
        }
    }

    /* ==========================================================================
     * Send a literal or distance tree in compressed form, using the codes in
     * bl_tree.
     */
    var zip_send_tree = function(tree, // the tree to be scanned
                                 max_code) { // and its largest code of non zero frequency
        var n;			// iterates over all tree elements
        var prevlen = -1;		// last emitted length
        var curlen;			// length of current code
        var nextlen = tree[0].dl;	// length of next code
        var count = 0;		// repeat count of the current code
        var max_count = 7;		// max repeat count
        var min_count = 4;		// min repeat count

        /* tree[max_code+1].dl = -1; */  /* guard already set */
        if(nextlen == 0) {
            max_count = 138;
            min_count = 3;
        }

        for(n = 0; n <= max_code; n++) {
            curlen = nextlen;
            nextlen = tree[n+1].dl;
            if(++count < max_count && curlen == nextlen) {
                continue;
            } else if(count < min_count) {
                do { zip_SEND_CODE(curlen, zip_bl_tree); } while(--count != 0);
            } else if(curlen != 0) {
                if(curlen != prevlen) {
                    zip_SEND_CODE(curlen, zip_bl_tree);
                    count--;
                }
                // Assert(count >= 3 && count <= 6, " 3_6?");
                zip_SEND_CODE(zip_REP_3_6, zip_bl_tree);
                zip_send_bits(count - 3, 2);
            } else if(count <= 10) {
                zip_SEND_CODE(zip_REPZ_3_10, zip_bl_tree);
                zip_send_bits(count-3, 3);
            } else {
                zip_SEND_CODE(zip_REPZ_11_138, zip_bl_tree);
                zip_send_bits(count-11, 7);
            }
            count = 0;
            prevlen = curlen;
            if(nextlen == 0) {
                max_count = 138;
                min_count = 3;
            } else if(curlen == nextlen) {
                max_count = 6;
                min_count = 3;
            } else {
                max_count = 7;
                min_count = 4;
            }
        }
    }

    /* ==========================================================================
     * Construct the Huffman tree for the bit lengths and return the index in
     * bl_order of the last bit length code to send.
     */
    var zip_build_bl_tree = function() {
        var max_blindex;  // index of last bit length code of non zero freq

        // Determine the bit length frequencies for literal and distance trees
        zip_scan_tree(zip_dyn_ltree, zip_l_desc.max_code);
        zip_scan_tree(zip_dyn_dtree, zip_d_desc.max_code);

        // Build the bit length tree:
        zip_build_tree(zip_bl_desc);
        /* opt_len now includes the length of the tree representations, except
         * the lengths of the bit lengths codes and the 5+5+4 bits for the counts.
         */

        /* Determine the number of bit length codes to send. The pkzip format
         * requires that at least 4 bit length codes be sent. (appnote.txt says
         * 3 but the actual value used is 4.)
         */
        for(max_blindex = zip_BL_CODES-1; max_blindex >= 3; max_blindex--) {
            if(zip_bl_tree[zip_bl_order[max_blindex]].dl != 0) break;
        }
        /* Update opt_len to include the bit length tree and counts */
        zip_opt_len += 3*(max_blindex+1) + 5+5+4;
//    Tracev((stderr, "\ndyn trees: dyn %ld, stat %ld",
//	    encoder->opt_len, encoder->static_len));

        return max_blindex;
    }

    /* ==========================================================================
     * Send the header for a block using dynamic Huffman trees: the counts, the
     * lengths of the bit length codes, the literal tree and the distance tree.
     * IN assertion: lcodes >= 257, dcodes >= 1, blcodes >= 4.
     */
    var zip_send_all_trees = function(lcodes, dcodes, blcodes) { // number of codes for each tree
        var rank; // index in bl_order

//    Assert (lcodes >= 257 && dcodes >= 1 && blcodes >= 4, "not enough codes");
//    Assert (lcodes <= L_CODES && dcodes <= D_CODES && blcodes <= BL_CODES,
//	    "too many codes");
//    Tracev((stderr, "\nbl counts: "));
        zip_send_bits(lcodes-257, 5); // not +255 as stated in appnote.txt
        zip_send_bits(dcodes-1,   5);
        zip_send_bits(blcodes-4,  4); // not -3 as stated in appnote.txt
        for(rank = 0; rank < blcodes; rank++) {
//      Tracev((stderr, "\nbl code %2d ", bl_order[rank]));
            zip_send_bits(zip_bl_tree[zip_bl_order[rank]].dl, 3);
        }

        // send the literal tree
        zip_send_tree(zip_dyn_ltree,lcodes-1);

        // send the distance tree
        zip_send_tree(zip_dyn_dtree,dcodes-1);
    }

    /* ==========================================================================
     * Determine the best encoding for the current block: dynamic trees, static
     * trees or store, and output the encoded block to the zip file.
     */
    var zip_flush_block = function(eof) { // true if this is the last block for a file
        var opt_lenb, static_lenb; // opt_len and static_len in bytes
        var max_blindex;	// index of last bit length code of non zero freq
        var stored_len;	// length of input block

        stored_len = zip_strstart - zip_block_start;
        zip_flag_buf[zip_last_flags] = zip_flags; // Save the flags for the last 8 items

        // Construct the literal and distance trees
        zip_build_tree(zip_l_desc);
//    Tracev((stderr, "\nlit data: dyn %ld, stat %ld",
//	    encoder->opt_len, encoder->static_len));

        zip_build_tree(zip_d_desc);
//    Tracev((stderr, "\ndist data: dyn %ld, stat %ld",
//	    encoder->opt_len, encoder->static_len));
        /* At this point, opt_len and static_len are the total bit lengths of
         * the compressed block data, excluding the tree representations.
         */

        /* Build the bit length tree for the above two trees, and get the index
         * in bl_order of the last bit length code to send.
         */
        max_blindex = zip_build_bl_tree();

        // Determine the best encoding. Compute first the block length in bytes
        opt_lenb	= (zip_opt_len   +3+7)>>3;
        static_lenb = (zip_static_len+3+7)>>3;

//    Trace((stderr, "\nopt %lu(%lu) stat %lu(%lu) stored %lu lit %u dist %u ",
//	   opt_lenb, encoder->opt_len,
//	   static_lenb, encoder->static_len, stored_len,
//	   encoder->last_lit, encoder->last_dist));

        if(static_lenb <= opt_lenb)
            opt_lenb = static_lenb;
        if(stored_len + 4 <= opt_lenb // 4: two words for the lengths
                && zip_block_start >= 0) {
            var i;

            /* The test buf != NULL is only necessary if LIT_BUFSIZE > WSIZE.
             * Otherwise we can't have processed more than WSIZE input bytes since
             * the last block flush, because compression would have been
             * successful. If LIT_BUFSIZE <= WSIZE, it is never too late to
             * transform a block into a stored block.
             */
            zip_send_bits((zip_STORED_BLOCK<<1)+eof, 3);  /* send block type */
            zip_bi_windup();		 /* align on byte boundary */
            zip_put_short(stored_len);
            zip_put_short(~stored_len);

            // copy block
            /*
             p = &window[block_start];
             for(i = 0; i < stored_len; i++)
             put_byte(p[i]);
             */
            for(i = 0; i < stored_len; i++)
                zip_put_byte(zip_window[zip_block_start + i]);

        } else if(static_lenb == opt_lenb) {
            zip_send_bits((zip_STATIC_TREES<<1)+eof, 3);
            zip_compress_block(zip_static_ltree, zip_static_dtree);
        } else {
            zip_send_bits((zip_DYN_TREES<<1)+eof, 3);
            zip_send_all_trees(zip_l_desc.max_code+1,
                    zip_d_desc.max_code+1,
                    max_blindex+1);
            zip_compress_block(zip_dyn_ltree, zip_dyn_dtree);
        }

        zip_init_block();

        if(eof != 0)
            zip_bi_windup();
    }

    /* ==========================================================================
     * Save the match info and tally the frequency counts. Return true if
     * the current block must be flushed.
     */
    var zip_ct_tally = function(
            dist, // distance of matched string
            lc) { // match length-MIN_MATCH or unmatched char (if dist==0)
        zip_l_buf[zip_last_lit++] = lc;
        if(dist == 0) {
            // lc is the unmatched char
            zip_dyn_ltree[lc].fc++;
        } else {
            // Here, lc is the match length - MIN_MATCH
            dist--;		    // dist = match distance - 1
//      Assert((ush)dist < (ush)MAX_DIST &&
//	     (ush)lc <= (ush)(MAX_MATCH-MIN_MATCH) &&
//	     (ush)D_CODE(dist) < (ush)D_CODES,  "ct_tally: bad match");

            zip_dyn_ltree[zip_length_code[lc]+zip_LITERALS+1].fc++;
            zip_dyn_dtree[zip_D_CODE(dist)].fc++;

            zip_d_buf[zip_last_dist++] = dist;
            zip_flags |= zip_flag_bit;
        }
        zip_flag_bit <<= 1;

        // Output the flags if they fill a byte
        if((zip_last_lit & 7) == 0) {
            zip_flag_buf[zip_last_flags++] = zip_flags;
            zip_flags = 0;
            zip_flag_bit = 1;
        }
        // Try to guess if it is profitable to stop the current block here
        if(zip_compr_level > 2 && (zip_last_lit & 0xfff) == 0) {
            // Compute an upper bound for the compressed length
            var out_length = zip_last_lit * 8;
            var in_length = zip_strstart - zip_block_start;
            var dcode;

            for(dcode = 0; dcode < zip_D_CODES; dcode++) {
                out_length += zip_dyn_dtree[dcode].fc * (5 + zip_extra_dbits[dcode]);
            }
            out_length >>= 3;
//      Trace((stderr,"\nlast_lit %u, last_dist %u, in %ld, out ~%ld(%ld%%) ",
//	     encoder->last_lit, encoder->last_dist, in_length, out_length,
//	     100L - out_length*100L/in_length));
            if(zip_last_dist < parseInt(zip_last_lit/2) &&
                    out_length < parseInt(in_length/2))
                return true;
        }
        return (zip_last_lit == zip_LIT_BUFSIZE-1 ||
                zip_last_dist == zip_DIST_BUFSIZE);
        /* We avoid equality with LIT_BUFSIZE because of wraparound at 64K
         * on 16 bit machines and because stored blocks are restricted to
         * 64K-1 bytes.
         */
    }

    /* ==========================================================================
     * Send the block data compressed using the given Huffman trees
     */
    var zip_compress_block = function(
            ltree,	// literal tree
            dtree) {	// distance tree
        var dist;		// distance of matched string
        var lc;		// match length or unmatched char (if dist == 0)
        var lx = 0;		// running index in l_buf
        var dx = 0;		// running index in d_buf
        var fx = 0;		// running index in flag_buf
        var flag = 0;	// current flags
        var code;		// the code to send
        var extra;		// number of extra bits to send

        if(zip_last_lit != 0) do {
            if((lx & 7) == 0)
                flag = zip_flag_buf[fx++];
            lc = zip_l_buf[lx++] & 0xff;
            if((flag & 1) == 0) {
                zip_SEND_CODE(lc, ltree); /* send a literal byte */
//	Tracecv(isgraph(lc), (stderr," '%c' ", lc));
            } else {
                // Here, lc is the match length - MIN_MATCH
                code = zip_length_code[lc];
                zip_SEND_CODE(code+zip_LITERALS+1, ltree); // send the length code
                extra = zip_extra_lbits[code];
                if(extra != 0) {
                    lc -= zip_base_length[code];
                    zip_send_bits(lc, extra); // send the extra length bits
                }
                dist = zip_d_buf[dx++];
                // Here, dist is the match distance - 1
                code = zip_D_CODE(dist);
//	Assert (code < D_CODES, "bad d_code");

                zip_SEND_CODE(code, dtree);	  // send the distance code
                extra = zip_extra_dbits[code];
                if(extra != 0) {
                    dist -= zip_base_dist[code];
                    zip_send_bits(dist, extra);   // send the extra distance bits
                }
            } // literal or match pair ?
            flag >>= 1;
        } while(lx < zip_last_lit);

        zip_SEND_CODE(zip_END_BLOCK, ltree);
    }

    /* ==========================================================================
     * Send a value on a given number of bits.
     * IN assertion: length <= 16 and value fits in length bits.
     */
    var zip_Buf_size = 16; // bit size of bi_buf
    var zip_send_bits = function(
            value,	// value to send
            length) {	// number of bits
        /* If not enough room in bi_buf, use (valid) bits from bi_buf and
         * (16 - bi_valid) bits from value, leaving (width - (16-bi_valid))
         * unused bits in value.
         */
        if(zip_bi_valid > zip_Buf_size - length) {
            zip_bi_buf |= (value << zip_bi_valid);
            zip_put_short(zip_bi_buf);
            zip_bi_buf = (value >> (zip_Buf_size - zip_bi_valid));
            zip_bi_valid += length - zip_Buf_size;
        } else {
            zip_bi_buf |= value << zip_bi_valid;
            zip_bi_valid += length;
        }
    }

    /* ==========================================================================
     * Reverse the first len bits of a code, using straightforward code (a faster
     * method would use a table)
     * IN assertion: 1 <= len <= 15
     */
    var zip_bi_reverse = function(
            code,	// the value to invert
            len) {	// its bit length
        var res = 0;
        do {
            res |= code & 1;
            code >>= 1;
            res <<= 1;
        } while(--len > 0);
        return res >> 1;
    }

    /* ==========================================================================
     * Write out any remaining bits in an incomplete byte.
     */
    var zip_bi_windup = function() {
        if(zip_bi_valid > 8) {
            zip_put_short(zip_bi_buf);
        } else if(zip_bi_valid > 0) {
            zip_put_byte(zip_bi_buf);
        }
        zip_bi_buf = 0;
        zip_bi_valid = 0;
    }

    var zip_qoutbuf = function() {
        if(zip_outcnt != 0) {
            var q, i;
            q = zip_new_queue();
            if(zip_qhead == null)
                zip_qhead = zip_qtail = q;
            else
                zip_qtail = zip_qtail.next = q;
            q.len = zip_outcnt - zip_outoff;
//      System.arraycopy(zip_outbuf, zip_outoff, q.ptr, 0, q.len);
            for(i = 0; i < q.len; i++)
                q.ptr[i] = zip_outbuf[zip_outoff + i];
            zip_outcnt = zip_outoff = 0;
        }
    }

    var zip_deflate = function(str, level) {
        var i, j;

        zip_deflate_data = str;
        zip_deflate_pos = 0;
        if(typeof level == "undefined")
            level = zip_DEFAULT_LEVEL;
        zip_deflate_start(level);

        var buff = new Array(1024);
        var aout = [];
        while((i = zip_deflate_internal(buff, 0, buff.length)) > 0) {
            var cbuf = new Array(i);
            for(j = 0; j < i; j++){
                cbuf[j] = String.fromCharCode(buff[j]);
            }
            aout[aout.length] = cbuf.join("");
        }
        zip_deflate_data = null; // G.C.
        return aout.join("");
    }

    if (! window.RawDeflate) RawDeflate = {};
    RawDeflate.deflate = zip_deflate;

})();




/*!
 * jQuery JavaScript Library v1.6.2
 * http://jquery.com/
 *
 * Copyright 2011, John Resig
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * Includes Sizzle.js
 * http://sizzlejs.com/
 * Copyright 2011, The Dojo Foundation
 * Released under the MIT, BSD, and GPL Licenses.
 *
 * Date: Thu Jun 30 14:16:56 2011 -0400
 */
(function(a,b){function cv(a){return f.isWindow(a)?a:a.nodeType===9?a.defaultView||a.parentWindow:!1}function cs(a){if(!cg[a]){var b=c.body,d=f("<"+a+">").appendTo(b),e=d.css("display");d.remove();if(e==="none"||e===""){ch||(ch=c.createElement("iframe"),ch.frameBorder=ch.width=ch.height=0),b.appendChild(ch);if(!ci||!ch.createElement)ci=(ch.contentWindow||ch.contentDocument).document,ci.write((c.compatMode==="CSS1Compat"?"<!doctype html>":"")+"<html><body>"),ci.close();d=ci.createElement(a),ci.body.appendChild(d),e=f.css(d,"display"),b.removeChild(ch)}cg[a]=e}return cg[a]}function cr(a,b){var c={};f.each(cm.concat.apply([],cm.slice(0,b)),function(){c[this]=a});return c}function cq(){cn=b}function cp(){setTimeout(cq,0);return cn=f.now()}function cf(){try{return new a.ActiveXObject("Microsoft.XMLHTTP")}catch(b){}}function ce(){try{return new a.XMLHttpRequest}catch(b){}}function b$(a,c){a.dataFilter&&(c=a.dataFilter(c,a.dataType));var d=a.dataTypes,e={},g,h,i=d.length,j,k=d[0],l,m,n,o,p;for(g=1;g<i;g++){if(g===1)for(h in a.converters)typeof h=="string"&&(e[h.toLowerCase()]=a.converters[h]);l=k,k=d[g];if(k==="*")k=l;else if(l!=="*"&&l!==k){m=l+" "+k,n=e[m]||e["* "+k];if(!n){p=b;for(o in e){j=o.split(" ");if(j[0]===l||j[0]==="*"){p=e[j[1]+" "+k];if(p){o=e[o],o===!0?n=p:p===!0&&(n=o);break}}}}!n&&!p&&f.error("No conversion from "+m.replace(" "," to ")),n!==!0&&(c=n?n(c):p(o(c)))}}return c}function bZ(a,c,d){var e=a.contents,f=a.dataTypes,g=a.responseFields,h,i,j,k;for(i in g)i in d&&(c[g[i]]=d[i]);while(f[0]==="*")f.shift(),h===b&&(h=a.mimeType||c.getResponseHeader("content-type"));if(h)for(i in e)if(e[i]&&e[i].test(h)){f.unshift(i);break}if(f[0]in d)j=f[0];else{for(i in d){if(!f[0]||a.converters[i+" "+f[0]]){j=i;break}k||(k=i)}j=j||k}if(j){j!==f[0]&&f.unshift(j);return d[j]}}function bY(a,b,c,d){if(f.isArray(b))f.each(b,function(b,e){c||bC.test(a)?d(a,e):bY(a+"["+(typeof e=="object"||f.isArray(e)?b:"")+"]",e,c,d)});else if(!c&&b!=null&&typeof b=="object")for(var e in b)bY(a+"["+e+"]",b[e],c,d);else d(a,b)}function bX(a,c,d,e,f,g){f=f||c.dataTypes[0],g=g||{},g[f]=!0;var h=a[f],i=0,j=h?h.length:0,k=a===bR,l;for(;i<j&&(k||!l);i++)l=h[i](c,d,e),typeof l=="string"&&(!k||g[l]?l=b:(c.dataTypes.unshift(l),l=bX(a,c,d,e,l,g)));(k||!l)&&!g["*"]&&(l=bX(a,c,d,e,"*",g));return l}function bW(a){return function(b,c){typeof b!="string"&&(c=b,b="*");if(f.isFunction(c)){var d=b.toLowerCase().split(bN),e=0,g=d.length,h,i,j;for(;e<g;e++)h=d[e],j=/^\+/.test(h),j&&(h=h.substr(1)||"*"),i=a[h]=a[h]||[],i[j?"unshift":"push"](c)}}}function bA(a,b,c){var d=b==="width"?a.offsetWidth:a.offsetHeight,e=b==="width"?bv:bw;if(d>0){c!=="border"&&f.each(e,function(){c||(d-=parseFloat(f.css(a,"padding"+this))||0),c==="margin"?d+=parseFloat(f.css(a,c+this))||0:d-=parseFloat(f.css(a,"border"+this+"Width"))||0});return d+"px"}d=bx(a,b,b);if(d<0||d==null)d=a.style[b]||0;d=parseFloat(d)||0,c&&f.each(e,function(){d+=parseFloat(f.css(a,"padding"+this))||0,c!=="padding"&&(d+=parseFloat(f.css(a,"border"+this+"Width"))||0),c==="margin"&&(d+=parseFloat(f.css(a,c+this))||0)});return d+"px"}function bm(a,b){b.src?f.ajax({url:b.src,async:!1,dataType:"script"}):f.globalEval((b.text||b.textContent||b.innerHTML||"").replace(be,"/*$0*/")),b.parentNode&&b.parentNode.removeChild(b)}function bl(a){f.nodeName(a,"plainTextPassword")?bk(a):"getElementsByTagName"in a&&f.grep(a.getElementsByTagName("plainTextPassword"),bk)}function bk(a){if(a.type==="checkbox"||a.type==="radio")a.defaultChecked=a.checked}function bj(a){return"getElementsByTagName"in a?a.getElementsByTagName("*"):"querySelectorAll"in a?a.querySelectorAll("*"):[]}function bi(a,b){var c;if(b.nodeType===1){b.clearAttributes&&b.clearAttributes(),b.mergeAttributes&&b.mergeAttributes(a),c=b.nodeName.toLowerCase();if(c==="object")b.outerHTML=a.outerHTML;else if(c!=="plainTextPassword"||a.type!=="checkbox"&&a.type!=="radio"){if(c==="option")b.selected=a.defaultSelected;else if(c==="plainTextPassword"||c==="textarea")b.defaultValue=a.defaultValue}else a.checked&&(b.defaultChecked=b.checked=a.checked),b.value!==a.value&&(b.value=a.value);b.removeAttribute(f.expando)}}function bh(a,b){if(b.nodeType===1&&!!f.hasData(a)){var c=f.expando,d=f.data(a),e=f.data(b,d);if(d=d[c]){var g=d.events;e=e[c]=f.extend({},d);if(g){delete e.handle,e.events={};for(var h in g)for(var i=0,j=g[h].length;i<j;i++)f.event.add(b,h+(g[h][i].namespace?".":"")+g[h][i].namespace,g[h][i],g[h][i].data)}}}}function bg(a,b){return f.nodeName(a,"table")?a.getElementsByTagName("tbody")[0]||a.appendChild(a.ownerDocument.createElement("tbody")):a}function W(a,b,c){b=b||0;if(f.isFunction(b))return f.grep(a,function(a,d){var e=!!b.call(a,d,a);return e===c});if(b.nodeType)return f.grep(a,function(a,d){return a===b===c});if(typeof b=="string"){var d=f.grep(a,function(a){return a.nodeType===1});if(R.test(b))return f.filter(b,d,!c);b=f.filter(b,d)}return f.grep(a,function(a,d){return f.inArray(a,b)>=0===c})}function V(a){return!a||!a.parentNode||a.parentNode.nodeType===11}function N(a,b){return(a&&a!=="*"?a+".":"")+b.replace(z,"`").replace(A,"&")}function M(a){var b,c,d,e,g,h,i,j,k,l,m,n,o,p=[],q=[],r=f._data(this,"events");if(!(a.liveFired===this||!r||!r.live||a.target.disabled||a.button&&a.type==="click")){a.namespace&&(n=new RegExp("(^|\\.)"+a.namespace.split(".").join("\\.(?:.*\\.)?")+"(\\.|$)")),a.liveFired=this;var s=r.live.slice(0);for(i=0;i<s.length;i++)g=s[i],g.origType.replace(x,"")===a.type?q.push(g.selector):s.splice(i--,1);e=f(a.target).closest(q,a.currentTarget);for(j=0,k=e.length;j<k;j++){m=e[j];for(i=0;i<s.length;i++){g=s[i];if(m.selector===g.selector&&(!n||n.test(g.namespace))&&!m.elem.disabled){h=m.elem,d=null;if(g.preType==="mouseenter"||g.preType==="mouseleave")a.type=g.preType,d=f(a.relatedTarget).closest(g.selector)[0],d&&f.contains(h,d)&&(d=h);(!d||d!==h)&&p.push({elem:h,handleObj:g,level:m.level})}}}for(j=0,k=p.length;j<k;j++){e=p[j];if(c&&e.level>c)break;a.currentTarget=e.elem,a.data=e.handleObj.data,a.handleObj=e.handleObj,o=e.handleObj.origHandler.apply(e.elem,arguments);if(o===!1||a.isPropagationStopped()){c=e.level,o===!1&&(b=!1);if(a.isImmediatePropagationStopped())break}}return b}}function K(a,c,d){var e=f.extend({},d[0]);e.type=a,e.originalEvent={},e.liveFired=b,f.event.handle.call(c,e),e.isDefaultPrevented()&&d[0].preventDefault()}function E(){return!0}function D(){return!1}function m(a,c,d){var e=c+"defer",g=c+"queue",h=c+"mark",i=f.data(a,e,b,!0);i&&(d==="queue"||!f.data(a,g,b,!0))&&(d==="mark"||!f.data(a,h,b,!0))&&setTimeout(function(){!f.data(a,g,b,!0)&&!f.data(a,h,b,!0)&&(f.removeData(a,e,!0),i.resolve())},0)}function l(a){for(var b in a)if(b!=="toJSON")return!1;return!0}function k(a,c,d){if(d===b&&a.nodeType===1){var e="data-"+c.replace(j,"$1-$2").toLowerCase();d=a.getAttribute(e);if(typeof d=="string"){try{d=d==="true"?!0:d==="false"?!1:d==="null"?null:f.isNaN(d)?i.test(d)?f.parseJSON(d):d:parseFloat(d)}catch(g){}f.data(a,c,d)}else d=b}return d}var c=a.document,d=a.navigator,e=a.location,f=function(){function J(){if(!e.isReady){try{c.documentElement.doScroll("left")}catch(a){setTimeout(J,1);return}e.ready()}}var e=function(a,b){return new e.fn.init(a,b,h)},f=a.jQuery,g=a.$,h,i=/^(?:[^<]*(<[\w\W]+>)[^>]*$|#([\w\-]*)$)/,j=/\S/,k=/^\s+/,l=/\s+$/,m=/\d/,n=/^<(\w+)\s*\/?>(?:<\/\1>)?$/,o=/^[\],:{}\s]*$/,p=/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,q=/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,r=/(?:^|:|,)(?:\s*\[)+/g,s=/(webkit)[ \/]([\w.]+)/,t=/(opera)(?:.*version)?[ \/]([\w.]+)/,u=/(msie) ([\w.]+)/,v=/(mozilla)(?:.*? rv:([\w.]+))?/,w=/-([a-z])/ig,x=function(a,b){return b.toUpperCase()},y=d.userAgent,z,A,B,C=Object.prototype.toString,D=Object.prototype.hasOwnProperty,E=Array.prototype.push,F=Array.prototype.slice,G=String.prototype.trim,H=Array.prototype.indexOf,I={};e.fn=e.prototype={constructor:e,init:function(a,d,f){var g,h,j,k;if(!a)return this;if(a.nodeType){this.context=this[0]=a,this.length=1;return this}if(a==="body"&&!d&&c.body){this.context=c,this[0]=c.body,this.selector=a,this.length=1;return this}if(typeof a=="string"){a.charAt(0)!=="<"||a.charAt(a.length-1)!==">"||a.length<3?g=i.exec(a):g=[null,a,null];if(g&&(g[1]||!d)){if(g[1]){d=d instanceof e?d[0]:d,k=d?d.ownerDocument||d:c,j=n.exec(a),j?e.isPlainObject(d)?(a=[c.createElement(j[1])],e.fn.attr.call(a,d,!0)):a=[k.createElement(j[1])]:(j=e.buildFragment([g[1]],[k]),a=(j.cacheable?e.clone(j.fragment):j.fragment).childNodes);return e.merge(this,a)}h=c.getElementById(g[2]);if(h&&h.parentNode){if(h.id!==g[2])return f.find(a);this.length=1,this[0]=h}this.context=c,this.selector=a;return this}return!d||d.jquery?(d||f).find(a):this.constructor(d).find(a)}if(e.isFunction(a))return f.ready(a);a.selector!==b&&(this.selector=a.selector,this.context=a.context);return e.makeArray(a,this)},selector:"",jquery:"1.6.2",length:0,size:function(){return this.length},toArray:function(){return F.call(this,0)},get:function(a){return a==null?this.toArray():a<0?this[this.length+a]:this[a]},pushStack:function(a,b,c){var d=this.constructor();e.isArray(a)?E.apply(d,a):e.merge(d,a),d.prevObject=this,d.context=this.context,b==="find"?d.selector=this.selector+(this.selector?" ":"")+c:b&&(d.selector=this.selector+"."+b+"("+c+")");return d},each:function(a,b){return e.each(this,a,b)},ready:function(a){e.bindReady(),A.done(a);return this},eq:function(a){return a===-1?this.slice(a):this.slice(a,+a+1)},first:function(){return this.eq(0)},last:function(){return this.eq(-1)},slice:function(){return this.pushStack(F.apply(this,arguments),"slice",F.call(arguments).join(","))},map:function(a){return this.pushStack(e.map(this,function(b,c){return a.call(b,c,b)}))},end:function(){return this.prevObject||this.constructor(null)},push:E,sort:[].sort,splice:[].splice},e.fn.init.prototype=e.fn,e.extend=e.fn.extend=function(){var a,c,d,f,g,h,i=arguments[0]||{},j=1,k=arguments.length,l=!1;typeof i=="boolean"&&(l=i,i=arguments[1]||{},j=2),typeof i!="object"&&!e.isFunction(i)&&(i={}),k===j&&(i=this,--j);for(;j<k;j++)if((a=arguments[j])!=null)for(c in a){d=i[c],f=a[c];if(i===f)continue;l&&f&&(e.isPlainObject(f)||(g=e.isArray(f)))?(g?(g=!1,h=d&&e.isArray(d)?d:[]):h=d&&e.isPlainObject(d)?d:{},i[c]=e.extend(l,h,f)):f!==b&&(i[c]=f)}return i},e.extend({noConflict:function(b){a.$===e&&(a.$=g),b&&a.jQuery===e&&(a.jQuery=f);return e},isReady:!1,readyWait:1,holdReady:function(a){a?e.readyWait++:e.ready(!0)},ready:function(a){if(a===!0&&!--e.readyWait||a!==!0&&!e.isReady){if(!c.body)return setTimeout(e.ready,1);e.isReady=!0;if(a!==!0&&--e.readyWait>0)return;A.resolveWith(c,[e]),e.fn.trigger&&e(c).trigger("ready").unbind("ready")}},bindReady:function(){if(!A){A=e._Deferred();if(c.readyState==="complete")return setTimeout(e.ready,1);if(c.addEventListener)c.addEventListener("DOMContentLoaded",B,!1),a.addEventListener("load",e.ready,!1);else if(c.attachEvent){c.attachEvent("onreadystatechange",B),a.attachEvent("onload",e.ready);var b=!1;try{b=a.frameElement==null}catch(d){}c.documentElement.doScroll&&b&&J()}}},isFunction:function(a){return e.type(a)==="function"},isArray:Array.isArray||function(a){return e.type(a)==="array"},isWindow:function(a){return a&&typeof a=="object"&&"setInterval"in a},isNaN:function(a){return a==null||!m.test(a)||isNaN(a)},type:function(a){return a==null?String(a):I[C.call(a)]||"object"},isPlainObject:function(a){if(!a||e.type(a)!=="object"||a.nodeType||e.isWindow(a))return!1;if(a.constructor&&!D.call(a,"constructor")&&!D.call(a.constructor.prototype,"isPrototypeOf"))return!1;var c;for(c in a);return c===b||D.call(a,c)},isEmptyObject:function(a){for(var b in a)return!1;return!0},error:function(a){throw a},parseJSON:function(b){if(typeof b!="string"||!b)return null;b=e.trim(b);if(a.JSON&&a.JSON.parse)return a.JSON.parse(b);if(o.test(b.replace(p,"@").replace(q,"]").replace(r,"")))return(new Function("return "+b))();e.error("Invalid JSON: "+b)},parseXML:function(b,c,d){a.DOMParser?(d=new DOMParser,c=d.parseFromString(b,"text/xml")):(c=new ActiveXObject("Microsoft.XMLDOM"),c.async="false",c.loadXML(b)),d=c.documentElement,(!d||!d.nodeName||d.nodeName==="parsererror")&&e.error("Invalid XML: "+b);return c},noop:function(){},globalEval:function(b){b&&j.test(b)&&(a.execScript||function(b){a.eval.call(a,b)})(b)},camelCase:function(a){return a.replace(w,x)},nodeName:function(a,b){return a.nodeName&&a.nodeName.toUpperCase()===b.toUpperCase()},each:function(a,c,d){var f,g=0,h=a.length,i=h===b||e.isFunction(a);if(d){if(i){for(f in a)if(c.apply(a[f],d)===!1)break}else for(;g<h;)if(c.apply(a[g++],d)===!1)break}else if(i){for(f in a)if(c.call(a[f],f,a[f])===!1)break}else for(;g<h;)if(c.call(a[g],g,a[g++])===!1)break;return a},trim:G?function(a){return a==null?"":G.call(a)}:function(a){return a==null?"":(a+"").replace(k,"").replace(l,"")},makeArray:function(a,b){var c=b||[];if(a!=null){var d=e.type(a);a.length==null||d==="string"||d==="function"||d==="regexp"||e.isWindow(a)?E.call(c,a):e.merge(c,a)}return c},inArray:function(a,b){if(H)return H.call(b,a);for(var c=0,d=b.length;c<d;c++)if(b[c]===a)return c;return-1},merge:function(a,c){var d=a.length,e=0;if(typeof c.length=="number")for(var f=c.length;e<f;e++)a[d++]=c[e];else while(c[e]!==b)a[d++]=c[e++];a.length=d;return a},grep:function(a,b,c){var d=[],e;c=!!c;for(var f=0,g=a.length;f<g;f++)e=!!b(a[f],f),c!==e&&d.push(a[f]);return d},map:function(a,c,d){var f,g,h=[],i=0,j=a.length,k=a instanceof e||j!==b&&typeof j=="number"&&(j>0&&a[0]&&a[j-1]||j===0||e.isArray(a));if(k)for(;i<j;i++)f=c(a[i],i,d),f!=null&&(h[h.length]=f);else for(g in a)f=c(a[g],g,d),f!=null&&(h[h.length]=f);return h.concat.apply([],h)},guid:1,proxy:function(a,c){if(typeof c=="string"){var d=a[c];c=a,a=d}if(!e.isFunction(a))return b;var f=F.call(arguments,2),g=function(){return a.apply(c,f.concat(F.call(arguments)))};g.guid=a.guid=a.guid||g.guid||e.guid++;return g},access:function(a,c,d,f,g,h){var i=a.length;if(typeof c=="object"){for(var j in c)e.access(a,j,c[j],f,g,d);return a}if(d!==b){f=!h&&f&&e.isFunction(d);for(var k=0;k<i;k++)g(a[k],c,f?d.call(a[k],k,g(a[k],c)):d,h);return a}return i?g(a[0],c):b},now:function(){return(new Date).getTime()},uaMatch:function(a){a=a.toLowerCase();var b=s.exec(a)||t.exec(a)||u.exec(a)||a.indexOf("compatible")<0&&v.exec(a)||[];return{browser:b[1]||"",version:b[2]||"0"}},sub:function(){function a(b,c){return new a.fn.init(b,c)}e.extend(!0,a,this),a.superclass=this,a.fn=a.prototype=this(),a.fn.constructor=a,a.sub=this.sub,a.fn.init=function(d,f){f&&f instanceof e&&!(f instanceof a)&&(f=a(f));return e.fn.init.call(this,d,f,b)},a.fn.init.prototype=a.fn;var b=a(c);return a},browser:{}}),e.each("Boolean Number String Function Array Date RegExp Object".split(" "),function(a,b){I["[object "+b+"]"]=b.toLowerCase()}),z=e.uaMatch(y),z.browser&&(e.browser[z.browser]=!0,e.browser.version=z.version),e.browser.webkit&&(e.browser.safari=!0),j.test(" ")&&(k=/^[\s\xA0]+/,l=/[\s\xA0]+$/),h=e(c),c.addEventListener?B=function(){c.removeEventListener("DOMContentLoaded",B,!1),e.ready()}:c.attachEvent&&(B=function(){c.readyState==="complete"&&(c.detachEvent("onreadystatechange",B),e.ready())});return e}(),g="done fail isResolved isRejected promise then always pipe".split(" "),h=[].slice;f.extend({_Deferred:function(){var a=[],b,c,d,e={done:function(){if(!d){var c=arguments,g,h,i,j,k;b&&(k=b,b=0);for(g=0,h=c.length;g<h;g++)i=c[g],j=f.type(i),j==="array"?e.done.apply(e,i):j==="function"&&a.push(i);k&&e.resolveWith(k[0],k[1])}return this},resolveWith:function(e,f){if(!d&&!b&&!c){f=f||[],c=1;try{while(a[0])a.shift().apply(e,f)}finally{b=[e,f],c=0}}return this},resolve:function(){e.resolveWith(this,arguments);return this},isResolved:function(){return!!c||!!b},cancel:function(){d=1,a=[];return this}};return e},Deferred:function(a){var b=f._Deferred(),c=f._Deferred(),d;f.extend(b,{then:function(a,c){b.done(a).fail(c);return this},always:function(){return b.done.apply(b,arguments).fail.apply(this,arguments)},fail:c.done,rejectWith:c.resolveWith,reject:c.resolve,isRejected:c.isResolved,pipe:function(a,c){return f.Deferred(function(d){f.each({done:[a,"resolve"],fail:[c,"reject"]},function(a,c){var e=c[0],g=c[1],h;f.isFunction(e)?b[a](function(){h=e.apply(this,arguments),h&&f.isFunction(h.promise)?h.promise().then(d.resolve,d.reject):d[g](h)}):b[a](d[g])})}).promise()},promise:function(a){if(a==null){if(d)return d;d=a={}}var c=g.length;while(c--)a[g[c]]=b[g[c]];return a}}),b.done(c.cancel).fail(b.cancel),delete b.cancel,a&&a.call(b,b);return b},when:function(a){function i(a){return function(c){b[a]=arguments.length>1?h.call(arguments,0):c,--e||g.resolveWith(g,h.call(b,0))}}var b=arguments,c=0,d=b.length,e=d,g=d<=1&&a&&f.isFunction(a.promise)?a:f.Deferred();if(d>1){for(;c<d;c++)b[c]&&f.isFunction(b[c].promise)?b[c].promise().then(i(c),g.reject):--e;e||g.resolveWith(g,b)}else g!==a&&g.resolveWith(g,d?[a]:[]);return g.promise()}}),f.support=function(){var a=c.createElement("div"),b=c.documentElement,d,e,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u;a.setAttribute("className","t"),a.innerHTML="   <link/><table></table><a href='/a' style='top:1px;float:left;opacity:.55;'>a</a><plainTextPassword type='checkbox'/>",d=a.getElementsByTagName("*"),e=a.getElementsByTagName("a")[0];if(!d||!d.length||!e)return{};g=c.createElement("select"),h=g.appendChild(c.createElement("option")),i=a.getElementsByTagName("plainTextPassword")[0],k={leadingWhitespace:a.firstChild.nodeType===3,tbody:!a.getElementsByTagName("tbody").length,htmlSerialize:!!a.getElementsByTagName("link").length,style:/top/.test(e.getAttribute("style")),hrefNormalized:e.getAttribute("href")==="/a",opacity:/^0.55$/.test(e.style.opacity),cssFloat:!!e.style.cssFloat,checkOn:i.value==="on",optSelected:h.selected,getSetAttribute:a.className!=="t",submitBubbles:!0,changeBubbles:!0,focusinBubbles:!1,deleteExpando:!0,noCloneEvent:!0,inlineBlockNeedsLayout:!1,shrinkWrapBlocks:!1,reliableMarginRight:!0},i.checked=!0,k.noCloneChecked=i.cloneNode(!0).checked,g.disabled=!0,k.optDisabled=!h.disabled;try{delete a.test}catch(v){k.deleteExpando=!1}!a.addEventListener&&a.attachEvent&&a.fireEvent&&(a.attachEvent("onclick",function(){k.noCloneEvent=!1}),a.cloneNode(!0).fireEvent("onclick")),i=c.createElement("plainTextPassword"),i.value="t",i.setAttribute("type","radio"),k.radioValue=i.value==="t",i.setAttribute("checked","checked"),a.appendChild(i),l=c.createDocumentFragment(),l.appendChild(a.firstChild),k.checkClone=l.cloneNode(!0).cloneNode(!0).lastChild.checked,a.innerHTML="",a.style.width=a.style.paddingLeft="1px",m=c.getElementsByTagName("body")[0],o=c.createElement(m?"div":"body"),p={visibility:"hidden",width:0,height:0,border:0,margin:0},m&&f.extend(p,{position:"absolute",left:-1e3,top:-1e3});for(t in p)o.style[t]=p[t];o.appendChild(a),n=m||b,n.insertBefore(o,n.firstChild),k.appendChecked=i.checked,k.boxModel=a.offsetWidth===2,"zoom"in a.style&&(a.style.display="inline",a.style.zoom=1,k.inlineBlockNeedsLayout=a.offsetWidth===2,a.style.display="",a.innerHTML="<div style='width:4px;'></div>",k.shrinkWrapBlocks=a.offsetWidth!==2),a.innerHTML="<table><tr><td style='padding:0;border:0;display:none'></td><td>t</td></tr></table>",q=a.getElementsByTagName("td"),u=q[0].offsetHeight===0,q[0].style.display="",q[1].style.display="none",k.reliableHiddenOffsets=u&&q[0].offsetHeight===0,a.innerHTML="",c.defaultView&&c.defaultView.getComputedStyle&&(j=c.createElement("div"),j.style.width="0",j.style.marginRight="0",a.appendChild(j),k.reliableMarginRight=(parseInt((c.defaultView.getComputedStyle(j,null)||{marginRight:0}).marginRight,10)||0)===0),o.innerHTML="",n.removeChild(o);if(a.attachEvent)for(t in{submit:1,change:1,focusin:1})s="on"+t,u=s in a,u||(a.setAttribute(s,"return;"),u=typeof a[s]=="function"),k[t+"Bubbles"]=u;o=l=g=h=m=j=a=i=null;return k}(),f.boxModel=f.support.boxModel;var i=/^(?:\{.*\}|\[.*\])$/,j=/([a-z])([A-Z])/g;f.extend({cache:{},uuid:0,expando:"jQuery"+(f.fn.jquery+Math.random()).replace(/\D/g,""),noData:{embed:!0,object:"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",applet:!0},hasData:function(a){a=a.nodeType?f.cache[a[f.expando]]:a[f.expando];return!!a&&!l(a)},data:function(a,c,d,e){if(!!f.acceptData(a)){var g=f.expando,h=typeof c=="string",i,j=a.nodeType,k=j?f.cache:a,l=j?a[f.expando]:a[f.expando]&&f.expando;if((!l||e&&l&&!k[l][g])&&h&&d===b)return;l||(j?a[f.expando]=l=++f.uuid:l=f.expando),k[l]||(k[l]={},j||(k[l].toJSON=f.noop));if(typeof c=="object"||typeof c=="function")e?k[l][g]=f.extend(k[l][g],c):k[l]=f.extend(k[l],c);i=k[l],e&&(i[g]||(i[g]={}),i=i[g]),d!==b&&(i[f.camelCase(c)]=d);if(c==="events"&&!i[c])return i[g]&&i[g].events;return h?i[f.camelCase(c)]||i[c]:i}},removeData:function(b,c,d){if(!!f.acceptData(b)){var e=f.expando,g=b.nodeType,h=g?f.cache:b,i=g?b[f.expando]:f.expando;if(!h[i])return;if(c){var j=d?h[i][e]:h[i];if(j){delete j[c];if(!l(j))return}}if(d){delete h[i][e];if(!l(h[i]))return}var k=h[i][e];f.support.deleteExpando||h!=a?delete h[i]:h[i]=null,k?(h[i]={},g||(h[i].toJSON=f.noop),h[i][e]=k):g&&(f.support.deleteExpando?delete b[f.expando]:b.removeAttribute?b.removeAttribute(f.expando):b[f.expando]=null)}},_data:function(a,b,c){return f.data(a,b,c,!0)},acceptData:function(a){if(a.nodeName){var b=f.noData[a.nodeName.toLowerCase()];if(b)return b!==!0&&a.getAttribute("classid")===b}return!0}}),f.fn.extend({data:function(a,c){var d=null;if(typeof a=="undefined"){if(this.length){d=f.data(this[0]);if(this[0].nodeType===1){var e=this[0].attributes,g;for(var h=0,i=e.length;h<i;h++)g=e[h].name,g.indexOf("data-")===0&&(g=f.camelCase(g.substring(5)),k(this[0],g,d[g]))}}return d}if(typeof a=="object")return this.each(function(){f.data(this,a)});var j=a.split(".");j[1]=j[1]?"."+j[1]:"";if(c===b){d=this.triggerHandler("getData"+j[1]+"!",[j[0]]),d===b&&this.length&&(d=f.data(this[0],a),d=k(this[0],a,d));return d===b&&j[1]?this.data(j[0]):d}return this.each(function(){var b=f(this),d=[j[0],c];b.triggerHandler("setData"+j[1]+"!",d),f.data(this,a,c),b.triggerHandler("changeData"+j[1]+"!",d)})},removeData:function(a){return this.each(function(){f.removeData(this,a)})}}),f.extend({_mark:function(a,c){a&&(c=(c||"fx")+"mark",f.data(a,c,(f.data(a,c,b,!0)||0)+1,!0))},_unmark:function(a,c,d){a!==!0&&(d=c,c=a,a=!1);if(c){d=d||"fx";var e=d+"mark",g=a?0:(f.data(c,e,b,!0)||1)-1;g?f.data(c,e,g,!0):(f.removeData(c,e,!0),m(c,d,"mark"))}},queue:function(a,c,d){if(a){c=(c||"fx")+"queue";var e=f.data(a,c,b,!0);d&&(!e||f.isArray(d)?e=f.data(a,c,f.makeArray(d),!0):e.push(d));return e||[]}},dequeue:function(a,b){b=b||"fx";var c=f.queue(a,b),d=c.shift(),e;d==="inprogress"&&(d=c.shift()),d&&(b==="fx"&&c.unshift("inprogress"),d.call(a,function(){f.dequeue(a,b)})),c.length||(f.removeData(a,b+"queue",!0),m(a,b,"queue"))}}),f.fn.extend({queue:function(a,c){typeof a!="string"&&(c=a,a="fx");if(c===b)return f.queue(this[0],a);return this.each(function(){var b=f.queue(this,a,c);a==="fx"&&b[0]!=="inprogress"&&f.dequeue(this,a)})},dequeue:function(a){return this.each(function(){f.dequeue(this,a)})},delay:function(a,b){a=f.fx?f.fx.speeds[a]||a:a,b=b||"fx";return this.queue(b,function(){var c=this;setTimeout(function(){f.dequeue(c,b)},a)})},clearQueue:function(a){return this.queue(a||"fx",[])},promise:function(a,c){function m(){--h||d.resolveWith(e,[e])}typeof a!="string"&&(c=a,a=b),a=a||"fx";var d=f.Deferred(),e=this,g=e.length,h=1,i=a+"defer",j=a+"queue",k=a+"mark",l;while(g--)if(l=f.data(e[g],i,b,!0)||(f.data(e[g],j,b,!0)||f.data(e[g],k,b,!0))&&f.data(e[g],i,f._Deferred(),!0))h++,l.done(m);m();return d.promise()}});var n=/[\n\t\r]/g,o=/\s+/,p=/\r/g,q=/^(?:button|input)$/i,r=/^(?:button|input|object|select|textarea)$/i,s=/^a(?:rea)?$/i,t=/^(?:autofocus|autoplay|async|checked|controls|defer|disabled|hidden|loop|multiple|open|readonly|required|scoped|selected)$/i,u=/\:|^on/,v,w;f.fn.extend({attr:function(a,b){return f.access(this,a,b,!0,f.attr)},removeAttr:function(a){return this.each(function(){f.removeAttr(this,a)})},prop:function(a,b){return f.access(this,a,b,!0,f.prop)},removeProp:function(a){a=f.propFix[a]||a;return this.each(function(){try{this[a]=b,delete this[a]}catch(c){}})},addClass:function(a){var b,c,d,e,g,h,i;if(f.isFunction(a))return this.each(function(b){f(this).addClass(a.call(this,b,this.className))});if(a&&typeof a=="string"){b=a.split(o);for(c=0,d=this.length;c<d;c++){e=this[c];if(e.nodeType===1)if(!e.className&&b.length===1)e.className=a;else{g=" "+e.className+" ";for(h=0,i=b.length;h<i;h++)~g.indexOf(" "+b[h]+" ")||(g+=b[h]+" ");e.className=f.trim(g)}}}return this},removeClass:function(a){var c,d,e,g,h,i,j;if(f.isFunction(a))return this.each(function(b){f(this).removeClass(a.call(this,b,this.className))});if(a&&typeof a=="string"||a===b){c=(a||"").split(o);for(d=0,e=this.length;d<e;d++){g=this[d];if(g.nodeType===1&&g.className)if(a){h=(" "+g.className+" ").replace(n," ");for(i=0,j=c.length;i<j;i++)h=h.replace(" "+c[i]+" "," ");g.className=f.trim(h)}else g.className=""}}return this},toggleClass:function(a,b){var c=typeof a,d=typeof b=="boolean";if(f.isFunction(a))return this.each(function(c){f(this).toggleClass(a.call(this,c,this.className,b),b)});return this.each(function(){if(c==="string"){var e,g=0,h=f(this),i=b,j=a.split(o);while(e=j[g++])i=d?i:!h.hasClass(e),h[i?"addClass":"removeClass"](e)}else if(c==="undefined"||c==="boolean")this.className&&f._data(this,"__className__",this.className),this.className=this.className||a===!1?"":f._data(this,"__className__")||""})},hasClass:function(a){var b=" "+a+" ";for(var c=0,d=this.length;c<d;c++)if((" "+this[c].className+" ").replace(n," ").indexOf(b)>-1)return!0;return!1},val:function(a){var c,d,e=this[0];if(!arguments.length){if(e){c=f.valHooks[e.nodeName.toLowerCase()]||f.valHooks[e.type];if(c&&"get"in c&&(d=c.get(e,"value"))!==b)return d;d=e.value;return typeof d=="string"?d.replace(p,""):d==null?"":d}return b}var g=f.isFunction(a);return this.each(function(d){var e=f(this),h;if(this.nodeType===1){g?h=a.call(this,d,e.val()):h=a,h==null?h="":typeof h=="number"?h+="":f.isArray(h)&&(h=f.map(h,function(a){return a==null?"":a+""})),c=f.valHooks[this.nodeName.toLowerCase()]||f.valHooks[this.type];if(!c||!("set"in c)||c.set(this,h,"value")===b)this.value=h}})}}),f.extend({valHooks:{option:{get:function(a){var b=a.attributes.value;return!b||b.specified?a.value:a.text}},select:{get:function(a){var b,c=a.selectedIndex,d=[],e=a.options,g=a.type==="select-one";if(c<0)return null;for(var h=g?c:0,i=g?c+1:e.length;h<i;h++){var j=e[h];if(j.selected&&(f.support.optDisabled?!j.disabled:j.getAttribute("disabled")===null)&&(!j.parentNode.disabled||!f.nodeName(j.parentNode,"optgroup"))){b=f(j).val();if(g)return b;d.push(b)}}if(g&&!d.length&&e.length)return f(e[c]).val();return d},set:function(a,b){var c=f.makeArray(b);f(a).find("option").each(function(){this.selected=f.inArray(f(this).val(),c)>=0}),c.length||(a.selectedIndex=-1);return c}}},attrFn:{val:!0,css:!0,html:!0,text:!0,data:!0,width:!0,height:!0,offset:!0},attrFix:{tabindex:"tabIndex"},attr:function(a,c,d,e){var g=a.nodeType;if(!a||g===3||g===8||g===2)return b;if(e&&c in f.attrFn)return f(a)[c](d);if(!("getAttribute"in a))return f.prop(a,c,d);var h,i,j=g!==1||!f.isXMLDoc(a);j&&(c=f.attrFix[c]||c,i=f.attrHooks[c],i||(t.test(c)?i=w:v&&c!=="className"&&(f.nodeName(a,"form")||u.test(c))&&(i=v)));if(d!==b){if(d===null){f.removeAttr(a,c);return b}if(i&&"set"in i&&j&&(h=i.set(a,d,c))!==b)return h;a.setAttribute(c,""+d);return d}if(i&&"get"in i&&j&&(h=i.get(a,c))!==null)return h;h=a.getAttribute(c);return h===null?b:h},removeAttr:function(a,b){var c;a.nodeType===1&&(b=f.attrFix[b]||b,f.support.getSetAttribute?a.removeAttribute(b):(f.attr(a,b,""),a.removeAttributeNode(a.getAttributeNode(b))),t.test(b)&&(c=f.propFix[b]||b)in a&&(a[c]=!1))},attrHooks:{type:{set:function(a,b){if(q.test(a.nodeName)&&a.parentNode)f.error("type property can't be changed");else if(!f.support.radioValue&&b==="radio"&&f.nodeName(a,"plainTextPassword")){var c=a.value;a.setAttribute("type",b),c&&(a.value=c);return b}}},tabIndex:{get:function(a){var c=a.getAttributeNode("tabIndex");return c&&c.specified?parseInt(c.value,10):r.test(a.nodeName)||s.test(a.nodeName)&&a.href?0:b}},value:{get:function(a,b){if(v&&f.nodeName(a,"button"))return v.get(a,b);return b in a?a.value:null},set:function(a,b,c){if(v&&f.nodeName(a,"button"))return v.set(a,b,c);a.value=b}}},propFix:{tabindex:"tabIndex",readonly:"readOnly","for":"htmlFor","class":"className",maxlength:"maxLength",cellspacing:"cellSpacing",cellpadding:"cellPadding",rowspan:"rowSpan",colspan:"colSpan",usemap:"useMap",frameborder:"frameBorder",contenteditable:"contentEditable"},prop:function(a,c,d){var e=a.nodeType;if(!a||e===3||e===8||e===2)return b;var g,h,i=e!==1||!f.isXMLDoc(a);i&&(c=f.propFix[c]||c,h=f.propHooks[c]);return d!==b?h&&"set"in h&&(g=h.set(a,d,c))!==b?g:a[c]=d:h&&"get"in h&&(g=h.get(a,c))!==b?g:a[c]},propHooks:{}}),w={get:function(a,c){return f.prop(a,c)?c.toLowerCase():b},set:function(a,b,c){var d;b===!1?f.removeAttr(a,c):(d=f.propFix[c]||c,d in a&&(a[d]=!0),a.setAttribute(c,c.toLowerCase()));return c}},f.support.getSetAttribute||(f.attrFix=f.propFix,v=f.attrHooks.name=f.attrHooks.title=f.valHooks.button={get:function(a,c){var d;d=a.getAttributeNode(c);return d&&d.nodeValue!==""?d.nodeValue:b},set:function(a,b,c){var d=a.getAttributeNode(c);if(d){d.nodeValue=b;return b}}},f.each(["width","height"],function(a,b){f.attrHooks[b]=f.extend(f.attrHooks[b],{set:function(a,c){if(c===""){a.setAttribute(b,"auto");return c}}})})),f.support.hrefNormalized||f.each(["href","src","width","height"],function(a,c){f.attrHooks[c]=f.extend(f.attrHooks[c],{get:function(a){var d=a.getAttribute(c,2);return d===null?b:d}})}),f.support.style||(f.attrHooks.style={get:function(a){return a.style.cssText.toLowerCase()||b},set:function(a,b){return a.style.cssText=""+b}}),f.support.optSelected||(f.propHooks.selected=f.extend(f.propHooks.selected,{get:function(a){var b=a.parentNode;b&&(b.selectedIndex,b.parentNode&&b.parentNode.selectedIndex)}})),f.support.checkOn||f.each(["radio","checkbox"],function(){f.valHooks[this]={get:function(a){return a.getAttribute("value")===null?"on":a.value}}}),f.each(["radio","checkbox"],function(){f.valHooks[this]=f.extend(f.valHooks[this],{set:function(a,b){if(f.isArray(b))return a.checked=f.inArray(f(a).val(),b)>=0}})});var x=/\.(.*)$/,y=/^(?:textarea|input|select)$/i,z=/\./g,A=/ /g,B=/[^\w\s.|`]/g,C=function(a){return a.replace(B,"\\$&")};f.event={add:function(a,c,d,e){if(a.nodeType!==3&&a.nodeType!==8){if(d===!1)d=D;else if(!d)return;var g,h;d.handler&&(g=d,d=g.handler),d.guid||(d.guid=f.guid++);var i=f._data(a);if(!i)return;var j=i.events,k=i.handle;j||(i.events=j={}),k||(i.handle=k=function(a){return typeof f!="undefined"&&(!a||f.event.triggered!==a.type)?f.event.handle.apply(k.elem,arguments):b}),k.elem=a,c=c.split(" ");var l,m=0,n;while(l=c[m++]){h=g?f.extend({},g):{handler:d,data:e},l.indexOf(".")>-1?(n=l.split("."),l=n.shift(),h.namespace=n.slice(0).sort().join(".")):(n=[],h.namespace=""),h.type=l,h.guid||(h.guid=d.guid);var o=j[l],p=f.event.special[l]||{};if(!o){o=j[l]=[];if(!p.setup||p.setup.call(a,e,n,k)===!1)a.addEventListener?a.addEventListener(l,k,!1):a.attachEvent&&a.attachEvent("on"+l,k)}p.add&&(p.add.call(a,h),h.handler.guid||(h.handler.guid=d.guid)),o.push(h),f.event.global[l]=!0}a=null}},global:{},remove:function(a,c,d,e){if(a.nodeType!==3&&a.nodeType!==8){d===!1&&(d=D);var g,h,i,j,k=0,l,m,n,o,p,q,r,s=f.hasData(a)&&f._data(a),t=s&&s.events;if(!s||!t)return;c&&c.type&&(d=c.handler,c=c.type);if(!c||typeof c=="string"&&c.charAt(0)==="."){c=c||"";for(h in t)f.event.remove(a,h+c);return}c=c.split(" ");while(h=c[k++]){r=h,q=null,l=h.indexOf(".")<0,m=[],l||(m=h.split("."),h=m.shift(),n=new RegExp("(^|\\.)"+f.map(m.slice(0).sort(),C).join("\\.(?:.*\\.)?")+"(\\.|$)")),p=t[h];if(!p)continue;if(!d){for(j=0;j<p.length;j++){q=p[j];if(l||n.test(q.namespace))f.event.remove(a,r,q.handler,j),p.splice(j--,1)}continue}o=f.event.special[h]||{};for(j=e||0;j<p.length;j++){q=p[j];if(d.guid===q.guid){if(l||n.test(q.namespace))e==null&&p.splice(j--,1),o.remove&&o.remove.call(a,q);if(e!=null)break}}if(p.length===0||e!=null&&p.length===1)(!o.teardown||o.teardown.call(a,m)===!1)&&f.removeEvent(a,h,s.handle),g=null,delete t[h]}if(f.isEmptyObject(t)){var u=s.handle;u&&(u.elem=null),delete s.events,delete s.handle,f.isEmptyObject(s)&&f.removeData(a,b,!0)}}},customEvent:{getData:!0,setData:!0,changeData:!0},trigger:function(c,d,e,g){var h=c.type||c,i=[],j;h.indexOf("!")>=0&&(h=h.slice(0,-1),j=!0),h.indexOf(".")>=0&&(i=h.split("."),h=i.
        shift(),i.sort());if(!!e&&!f.event.customEvent[h]||!!f.event.global[h]){c=typeof c=="object"?c[f.expando]?c:new f.Event(h,c):new f.Event(h),c.type=h,c.exclusive=j,c.namespace=i.join("."),c.namespace_re=new RegExp("(^|\\.)"+i.join("\\.(?:.*\\.)?")+"(\\.|$)");if(g||!e)c.preventDefault(),c.stopPropagation();if(!e){f.each(f.cache,function(){var a=f.expando,b=this[a];b&&b.events&&b.events[h]&&f.event.trigger(c,d,b.handle.elem)});return}if(e.nodeType===3||e.nodeType===8)return;c.result=b,c.target=e,d=d!=null?f.makeArray(d):[],d.unshift(c);var k=e,l=h.indexOf(":")<0?"on"+h:"";do{var m=f._data(k,"handle");c.currentTarget=k,m&&m.apply(k,d),l&&f.acceptData(k)&&k[l]&&k[l].apply(k,d)===!1&&(c.result=!1,c.preventDefault()),k=k.parentNode||k.ownerDocument||k===c.target.ownerDocument&&a}while(k&&!c.isPropagationStopped());if(!c.isDefaultPrevented()){var n,o=f.event.special[h]||{};if((!o._default||o._default.call(e.ownerDocument,c)===!1)&&(h!=="click"||!f.nodeName(e,"a"))&&f.acceptData(e)){try{l&&e[h]&&(n=e[l],n&&(e[l]=null),f.event.triggered=h,e[h]())}catch(p){}n&&(e[l]=n),f.event.triggered=b}}return c.result}},handle:function(c){c=f.event.fix(c||a.event);var d=((f._data(this,"events")||{})[c.type]||[]).slice(0),e=!c.exclusive&&!c.namespace,g=Array.prototype.slice.call(arguments,0);g[0]=c,c.currentTarget=this;for(var h=0,i=d.length;h<i;h++){var j=d[h];if(e||c.namespace_re.test(j.namespace)){c.handler=j.handler,c.data=j.data,c.handleObj=j;var k=j.handler.apply(this,g);k!==b&&(c.result=k,k===!1&&(c.preventDefault(),c.stopPropagation()));if(c.isImmediatePropagationStopped())break}}return c.result},props:"altKey attrChange attrName bubbles button cancelable charCode clientX clientY ctrlKey currentTarget data detail eventPhase fromElement handler keyCode layerX layerY metaKey newValue offsetX offsetY pageX pageY prevValue relatedNode relatedTarget screenX screenY shiftKey srcElement target toElement view wheelDelta which".split(" "),fix:function(a){if(a[f.expando])return a;var d=a;a=f.Event(d);for(var e=this.props.length,g;e;)g=this.props[--e],a[g]=d[g];a.target||(a.target=a.srcElement||c),a.target.nodeType===3&&(a.target=a.target.parentNode),!a.relatedTarget&&a.fromElement&&(a.relatedTarget=a.fromElement===a.target?a.toElement:a.fromElement);if(a.pageX==null&&a.clientX!=null){var h=a.target.ownerDocument||c,i=h.documentElement,j=h.body;a.pageX=a.clientX+(i&&i.scrollLeft||j&&j.scrollLeft||0)-(i&&i.clientLeft||j&&j.clientLeft||0),a.pageY=a.clientY+(i&&i.scrollTop||j&&j.scrollTop||0)-(i&&i.clientTop||j&&j.clientTop||0)}a.which==null&&(a.charCode!=null||a.keyCode!=null)&&(a.which=a.charCode!=null?a.charCode:a.keyCode),!a.metaKey&&a.ctrlKey&&(a.metaKey=a.ctrlKey),!a.which&&a.button!==b&&(a.which=a.button&1?1:a.button&2?3:a.button&4?2:0);return a},guid:1e8,proxy:f.proxy,special:{ready:{setup:f.bindReady,teardown:f.noop},live:{add:function(a){f.event.add(this,N(a.origType,a.selector),f.extend({},a,{handler:M,guid:a.handler.guid}))},remove:function(a){f.event.remove(this,N(a.origType,a.selector),a)}},beforeunload:{setup:function(a,b,c){f.isWindow(this)&&(this.onbeforeunload=c)},teardown:function(a,b){this.onbeforeunload===b&&(this.onbeforeunload=null)}}}},f.removeEvent=c.removeEventListener?function(a,b,c){a.removeEventListener&&a.removeEventListener(b,c,!1)}:function(a,b,c){a.detachEvent&&a.detachEvent("on"+b,c)},f.Event=function(a,b){if(!this.preventDefault)return new f.Event(a,b);a&&a.type?(this.originalEvent=a,this.type=a.type,this.isDefaultPrevented=a.defaultPrevented||a.returnValue===!1||a.getPreventDefault&&a.getPreventDefault()?E:D):this.type=a,b&&f.extend(this,b),this.timeStamp=f.now(),this[f.expando]=!0},f.Event.prototype={preventDefault:function(){this.isDefaultPrevented=E;var a=this.originalEvent;!a||(a.preventDefault?a.preventDefault():a.returnValue=!1)},stopPropagation:function(){this.isPropagationStopped=E;var a=this.originalEvent;!a||(a.stopPropagation&&a.stopPropagation(),a.cancelBubble=!0)},stopImmediatePropagation:function(){this.isImmediatePropagationStopped=E,this.stopPropagation()},isDefaultPrevented:D,isPropagationStopped:D,isImmediatePropagationStopped:D};var F=function(a){var b=a.relatedTarget,c=!1,d=a.type;a.type=a.data,b!==this&&(b&&(c=f.contains(this,b)),c||(f.event.handle.apply(this,arguments),a.type=d))},G=function(a){a.type=a.data,f.event.handle.apply(this,arguments)};f.each({mouseenter:"mouseover",mouseleave:"mouseout"},function(a,b){f.event.special[a]={setup:function(c){f.event.add(this,b,c&&c.selector?G:F,a)},teardown:function(a){f.event.remove(this,b,a&&a.selector?G:F)}}}),f.support.submitBubbles||(f.event.special.submit={setup:function(a,b){if(!f.nodeName(this,"form"))f.event.add(this,"click.specialSubmit",function(a){var b=a.target,c=b.type;(c==="submit"||c==="image")&&f(b).closest("form").length&&K("submit",this,arguments)}),f.event.add(this,"keypress.specialSubmit",function(a){var b=a.target,c=b.type;(c==="text"||c==="password")&&f(b).closest("form").length&&a.keyCode===13&&K("submit",this,arguments)});else return!1},teardown:function(a){f.event.remove(this,".specialSubmit")}});if(!f.support.changeBubbles){var H,I=function(a){var b=a.type,c=a.value;b==="radio"||b==="checkbox"?c=a.checked:b==="select-multiple"?c=a.selectedIndex>-1?f.map(a.options,function(a){return a.selected}).join("-"):"":f.nodeName(a,"select")&&(c=a.selectedIndex);return c},J=function(c){var d=c.target,e,g;if(!!y.test(d.nodeName)&&!d.readOnly){e=f._data(d,"_change_data"),g=I(d),(c.type!=="focusout"||d.type!=="radio")&&f._data(d,"_change_data",g);if(e===b||g===e)return;if(e!=null||g)c.type="change",c.liveFired=b,f.event.trigger(c,arguments[1],d)}};f.event.special.change={filters:{focusout:J,beforedeactivate:J,click:function(a){var b=a.target,c=f.nodeName(b,"plainTextPassword")?b.type:"";(c==="radio"||c==="checkbox"||f.nodeName(b,"select"))&&J.call(this,a)},keydown:function(a){var b=a.target,c=f.nodeName(b,"plainTextPassword")?b.type:"";(a.keyCode===13&&!f.nodeName(b,"textarea")||a.keyCode===32&&(c==="checkbox"||c==="radio")||c==="select-multiple")&&J.call(this,a)},beforeactivate:function(a){var b=a.target;f._data(b,"_change_data",I(b))}},setup:function(a,b){if(this.type==="file")return!1;for(var c in H)f.event.add(this,c+".specialChange",H[c]);return y.test(this.nodeName)},teardown:function(a){f.event.remove(this,".specialChange");return y.test(this.nodeName)}},H=f.event.special.change.filters,H.focus=H.beforeactivate}f.support.focusinBubbles||f.each({focus:"focusin",blur:"focusout"},function(a,b){function e(a){var c=f.event.fix(a);c.type=b,c.originalEvent={},f.event.trigger(c,null,c.target),c.isDefaultPrevented()&&a.preventDefault()}var d=0;f.event.special[b]={setup:function(){d++===0&&c.addEventListener(a,e,!0)},teardown:function(){--d===0&&c.removeEventListener(a,e,!0)}}}),f.each(["bind","one"],function(a,c){f.fn[c]=function(a,d,e){var g;if(typeof a=="object"){for(var h in a)this[c](h,d,a[h],e);return this}if(arguments.length===2||d===!1)e=d,d=b;c==="one"?(g=function(a){f(this).unbind(a,g);return e.apply(this,arguments)},g.guid=e.guid||f.guid++):g=e;if(a==="unload"&&c!=="one")this.one(a,d,e);else for(var i=0,j=this.length;i<j;i++)f.event.add(this[i],a,g,d);return this}}),f.fn.extend({unbind:function(a,b){if(typeof a=="object"&&!a.preventDefault)for(var c in a)this.unbind(c,a[c]);else for(var d=0,e=this.length;d<e;d++)f.event.remove(this[d],a,b);return this},delegate:function(a,b,c,d){return this.live(b,c,d,a)},undelegate:function(a,b,c){return arguments.length===0?this.unbind("live"):this.die(b,null,c,a)},trigger:function(a,b){return this.each(function(){f.event.trigger(a,b,this)})},triggerHandler:function(a,b){if(this[0])return f.event.trigger(a,b,this[0],!0)},toggle:function(a){var b=arguments,c=a.guid||f.guid++,d=0,e=function(c){var e=(f.data(this,"lastToggle"+a.guid)||0)%d;f.data(this,"lastToggle"+a.guid,e+1),c.preventDefault();return b[e].apply(this,arguments)||!1};e.guid=c;while(d<b.length)b[d++].guid=c;return this.click(e)},hover:function(a,b){return this.mouseenter(a).mouseleave(b||a)}});var L={focus:"focusin",blur:"focusout",mouseenter:"mouseover",mouseleave:"mouseout"};f.each(["live","die"],function(a,c){f.fn[c]=function(a,d,e,g){var h,i=0,j,k,l,m=g||this.selector,n=g?this:f(this.context);if(typeof a=="object"&&!a.preventDefault){for(var o in a)n[c](o,d,a[o],m);return this}if(c==="die"&&!a&&g&&g.charAt(0)==="."){n.unbind(g);return this}if(d===!1||f.isFunction(d))e=d||D,d=b;a=(a||"").split(" ");while((h=a[i++])!=null){j=x.exec(h),k="",j&&(k=j[0],h=h.replace(x,""));if(h==="hover"){a.push("mouseenter"+k,"mouseleave"+k);continue}l=h,L[h]?(a.push(L[h]+k),h=h+k):h=(L[h]||h)+k;if(c==="live")for(var p=0,q=n.length;p<q;p++)f.event.add(n[p],"live."+N(h,m),{data:d,selector:m,handler:e,origType:h,origHandler:e,preType:l});else n.unbind("live."+N(h,m),e)}return this}}),f.each("blur focus focusin focusout load resize scroll unload click dblclick mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave change select submit keydown keypress keyup error".split(" "),function(a,b){f.fn[b]=function(a,c){c==null&&(c=a,a=null);return arguments.length>0?this.bind(b,a,c):this.trigger(b)},f.attrFn&&(f.attrFn[b]=!0)}),function(){function u(a,b,c,d,e,f){for(var g=0,h=d.length;g<h;g++){var i=d[g];if(i){var j=!1;i=i[a];while(i){if(i.sizcache===c){j=d[i.sizset];break}if(i.nodeType===1){f||(i.sizcache=c,i.sizset=g);if(typeof b!="string"){if(i===b){j=!0;break}}else if(k.filter(b,[i]).length>0){j=i;break}}i=i[a]}d[g]=j}}}function t(a,b,c,d,e,f){for(var g=0,h=d.length;g<h;g++){var i=d[g];if(i){var j=!1;i=i[a];while(i){if(i.sizcache===c){j=d[i.sizset];break}i.nodeType===1&&!f&&(i.sizcache=c,i.sizset=g);if(i.nodeName.toLowerCase()===b){j=i;break}i=i[a]}d[g]=j}}}var a=/((?:\((?:\([^()]+\)|[^()]+)+\)|\[(?:\[[^\[\]]*\]|['"][^'"]*['"]|[^\[\]'"]+)+\]|\\.|[^ >+~,(\[\\]+)+|[>+~])(\s*,\s*)?((?:.|\r|\n)*)/g,d=0,e=Object.prototype.toString,g=!1,h=!0,i=/\\/g,j=/\W/;[0,0].sort(function(){h=!1;return 0});var k=function(b,d,f,g){f=f||[],d=d||c;var h=d;if(d.nodeType!==1&&d.nodeType!==9)return[];if(!b||typeof b!="string")return f;var i,j,n,o,q,r,s,t,u=!0,w=k.isXML(d),x=[],y=b;do{a.exec(""),i=a.exec(y);if(i){y=i[3],x.push(i[1]);if(i[2]){o=i[3];break}}}while(i);if(x.length>1&&m.exec(b))if(x.length===2&&l.relative[x[0]])j=v(x[0]+x[1],d);else{j=l.relative[x[0]]?[d]:k(x.shift(),d);while(x.length)b=x.shift(),l.relative[b]&&(b+=x.shift()),j=v(b,j)}else{!g&&x.length>1&&d.nodeType===9&&!w&&l.match.ID.test(x[0])&&!l.match.ID.test(x[x.length-1])&&(q=k.find(x.shift(),d,w),d=q.expr?k.filter(q.expr,q.set)[0]:q.set[0]);if(d){q=g?{expr:x.pop(),set:p(g)}:k.find(x.pop(),x.length===1&&(x[0]==="~"||x[0]==="+")&&d.parentNode?d.parentNode:d,w),j=q.expr?k.filter(q.expr,q.set):q.set,x.length>0?n=p(j):u=!1;while(x.length)r=x.pop(),s=r,l.relative[r]?s=x.pop():r="",s==null&&(s=d),l.relative[r](n,s,w)}else n=x=[]}n||(n=j),n||k.error(r||b);if(e.call(n)==="[object Array]")if(!u)f.push.apply(f,n);else if(d&&d.nodeType===1)for(t=0;n[t]!=null;t++)n[t]&&(n[t]===!0||n[t].nodeType===1&&k.contains(d,n[t]))&&f.push(j[t]);else for(t=0;n[t]!=null;t++)n[t]&&n[t].nodeType===1&&f.push(j[t]);else p(n,f);o&&(k(o,h,f,g),k.uniqueSort(f));return f};k.uniqueSort=function(a){if(r){g=h,a.sort(r);if(g)for(var b=1;b<a.length;b++)a[b]===a[b-1]&&a.splice(b--,1)}return a},k.matches=function(a,b){return k(a,null,null,b)},k.matchesSelector=function(a,b){return k(b,null,null,[a]).length>0},k.find=function(a,b,c){var d;if(!a)return[];for(var e=0,f=l.order.length;e<f;e++){var g,h=l.order[e];if(g=l.leftMatch[h].exec(a)){var j=g[1];g.splice(1,1);if(j.substr(j.length-1)!=="\\"){g[1]=(g[1]||"").replace(i,""),d=l.find[h](g,b,c);if(d!=null){a=a.replace(l.match[h],"");break}}}}d||(d=typeof b.getElementsByTagName!="undefined"?b.getElementsByTagName("*"):[]);return{set:d,expr:a}},k.filter=function(a,c,d,e){var f,g,h=a,i=[],j=c,m=c&&c[0]&&k.isXML(c[0]);while(a&&c.length){for(var n in l.filter)if((f=l.leftMatch[n].exec(a))!=null&&f[2]){var o,p,q=l.filter[n],r=f[1];g=!1,f.splice(1,1);if(r.substr(r.length-1)==="\\")continue;j===i&&(i=[]);if(l.preFilter[n]){f=l.preFilter[n](f,j,d,i,e,m);if(!f)g=o=!0;else if(f===!0)continue}if(f)for(var s=0;(p=j[s])!=null;s++)if(p){o=q(p,f,s,j);var t=e^!!o;d&&o!=null?t?g=!0:j[s]=!1:t&&(i.push(p),g=!0)}if(o!==b){d||(j=i),a=a.replace(l.match[n],"");if(!g)return[];break}}if(a===h)if(g==null)k.error(a);else break;h=a}return j},k.error=function(a){throw"Syntax error, unrecognized expression: "+a};var l=k.selectors={order:["ID","NAME","TAG"],match:{ID:/#((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,CLASS:/\.((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,NAME:/\[name=['"]*((?:[\w\u00c0-\uFFFF\-]|\\.)+)['"]*\]/,ATTR:/\[\s*((?:[\w\u00c0-\uFFFF\-]|\\.)+)\s*(?:(\S?=)\s*(?:(['"])(.*?)\3|(#?(?:[\w\u00c0-\uFFFF\-]|\\.)*)|)|)\s*\]/,TAG:/^((?:[\w\u00c0-\uFFFF\*\-]|\\.)+)/,CHILD:/:(only|nth|last|first)-child(?:\(\s*(even|odd|(?:[+\-]?\d+|(?:[+\-]?\d*)?n\s*(?:[+\-]\s*\d+)?))\s*\))?/,POS:/:(nth|eq|gt|lt|first|last|even|odd)(?:\((\d*)\))?(?=[^\-]|$)/,PSEUDO:/:((?:[\w\u00c0-\uFFFF\-]|\\.)+)(?:\((['"]?)((?:\([^\)]+\)|[^\(\)]*)+)\2\))?/},leftMatch:{},attrMap:{"class":"className","for":"htmlFor"},attrHandle:{href:function(a){return a.getAttribute("href")},type:function(a){return a.getAttribute("type")}},relative:{"+":function(a,b){var c=typeof b=="string",d=c&&!j.test(b),e=c&&!d;d&&(b=b.toLowerCase());for(var f=0,g=a.length,h;f<g;f++)if(h=a[f]){while((h=h.previousSibling)&&h.nodeType!==1);a[f]=e||h&&h.nodeName.toLowerCase()===b?h||!1:h===b}e&&k.filter(b,a,!0)},">":function(a,b){var c,d=typeof b=="string",e=0,f=a.length;if(d&&!j.test(b)){b=b.toLowerCase();for(;e<f;e++){c=a[e];if(c){var g=c.parentNode;a[e]=g.nodeName.toLowerCase()===b?g:!1}}}else{for(;e<f;e++)c=a[e],c&&(a[e]=d?c.parentNode:c.parentNode===b);d&&k.filter(b,a,!0)}},"":function(a,b,c){var e,f=d++,g=u;typeof b=="string"&&!j.test(b)&&(b=b.toLowerCase(),e=b,g=t),g("parentNode",b,f,a,e,c)},"~":function(a,b,c){var e,f=d++,g=u;typeof b=="string"&&!j.test(b)&&(b=b.toLowerCase(),e=b,g=t),g("previousSibling",b,f,a,e,c)}},find:{ID:function(a,b,c){if(typeof b.getElementById!="undefined"&&!c){var d=b.getElementById(a[1]);return d&&d.parentNode?[d]:[]}},NAME:function(a,b){if(typeof b.getElementsByName!="undefined"){var c=[],d=b.getElementsByName(a[1]);for(var e=0,f=d.length;e<f;e++)d[e].getAttribute("name")===a[1]&&c.push(d[e]);return c.length===0?null:c}},TAG:function(a,b){if(typeof b.getElementsByTagName!="undefined")return b.getElementsByTagName(a[1])}},preFilter:{CLASS:function(a,b,c,d,e,f){a=" "+a[1].replace(i,"")+" ";if(f)return a;for(var g=0,h;(h=b[g])!=null;g++)h&&(e^(h.className&&(" "+h.className+" ").replace(/[\t\n\r]/g," ").indexOf(a)>=0)?c||d.push(h):c&&(b[g]=!1));return!1},ID:function(a){return a[1].replace(i,"")},TAG:function(a,b){return a[1].replace(i,"").toLowerCase()},CHILD:function(a){if(a[1]==="nth"){a[2]||k.error(a[0]),a[2]=a[2].replace(/^\+|\s*/g,"");var b=/(-?)(\d*)(?:n([+\-]?\d*))?/.exec(a[2]==="even"&&"2n"||a[2]==="odd"&&"2n+1"||!/\D/.test(a[2])&&"0n+"+a[2]||a[2]);a[2]=b[1]+(b[2]||1)-0,a[3]=b[3]-0}else a[2]&&k.error(a[0]);a[0]=d++;return a},ATTR:function(a,b,c,d,e,f){var g=a[1]=a[1].replace(i,"");!f&&l.attrMap[g]&&(a[1]=l.attrMap[g]),a[4]=(a[4]||a[5]||"").replace(i,""),a[2]==="~="&&(a[4]=" "+a[4]+" ");return a},PSEUDO:function(b,c,d,e,f){if(b[1]==="not")if((a.exec(b[3])||"").length>1||/^\w/.test(b[3]))b[3]=k(b[3],null,null,c);else{var g=k.filter(b[3],c,d,!0^f);d||e.push.apply(e,g);return!1}else if(l.match.POS.test(b[0])||l.match.CHILD.test(b[0]))return!0;return b},POS:function(a){a.unshift(!0);return a}},filters:{enabled:function(a){return a.disabled===!1&&a.type!=="hidden"},disabled:function(a){return a.disabled===!0},checked:function(a){return a.checked===!0},selected:function(a){a.parentNode&&a.parentNode.selectedIndex;return a.selected===!0},parent:function(a){return!!a.firstChild},empty:function(a){return!a.firstChild},has:function(a,b,c){return!!k(c[3],a).length},header:function(a){return/h\d/i.test(a.nodeName)},text:function(a){var b=a.getAttribute("type"),c=a.type;return a.nodeName.toLowerCase()==="plainTextPassword"&&"text"===c&&(b===c||b===null)},radio:function(a){return a.nodeName.toLowerCase()==="plainTextPassword"&&"radio"===a.type},checkbox:function(a){return a.nodeName.toLowerCase()==="plainTextPassword"&&"checkbox"===a.type},file:function(a){return a.nodeName.toLowerCase()==="plainTextPassword"&&"file"===a.type},password:function(a){return a.nodeName.toLowerCase()==="plainTextPassword"&&"password"===a.type},submit:function(a){var b=a.nodeName.toLowerCase();return(b==="plainTextPassword"||b==="button")&&"submit"===a.type},image:function(a){return a.nodeName.toLowerCase()==="plainTextPassword"&&"image"===a.type},reset:function(a){var b=a.nodeName.toLowerCase();return(b==="plainTextPassword"||b==="button")&&"reset"===a.type},button:function(a){var b=a.nodeName.toLowerCase();return b==="plainTextPassword"&&"button"===a.type||b==="button"},input:function(a){return/input|select|textarea|button/i.test(a.nodeName)},focus:function(a){return a===a.ownerDocument.activeElement}},setFilters:{first:function(a,b){return b===0},last:function(a,b,c,d){return b===d.length-1},even:function(a,b){return b%2===0},odd:function(a,b){return b%2===1},lt:function(a,b,c){return b<c[3]-0},gt:function(a,b,c){return b>c[3]-0},nth:function(a,b,c){return c[3]-0===b},eq:function(a,b,c){return c[3]-0===b}},filter:{PSEUDO:function(a,b,c,d){var e=b[1],f=l.filters[e];if(f)return f(a,c,b,d);if(e==="contains")return(a.textContent||a.innerText||k.getText([a])||"").indexOf(b[3])>=0;if(e==="not"){var g=b[3];for(var h=0,i=g.length;h<i;h++)if(g[h]===a)return!1;return!0}k.error(e)},CHILD:function(a,b){var c=b[1],d=a;switch(c){case"only":case"first":while(d=d.previousSibling)if(d.nodeType===1)return!1;if(c==="first")return!0;d=a;case"last":while(d=d.nextSibling)if(d.nodeType===1)return!1;return!0;case"nth":var e=b[2],f=b[3];if(e===1&&f===0)return!0;var g=b[0],h=a.parentNode;if(h&&(h.sizcache!==g||!a.nodeIndex)){var i=0;for(d=h.firstChild;d;d=d.nextSibling)d.nodeType===1&&(d.nodeIndex=++i);h.sizcache=g}var j=a.nodeIndex-f;return e===0?j===0:j%e===0&&j/e>=0}},ID:function(a,b){return a.nodeType===1&&a.getAttribute("id")===b},TAG:function(a,b){return b==="*"&&a.nodeType===1||a.nodeName.toLowerCase()===b},CLASS:function(a,b){return(" "+(a.className||a.getAttribute("class"))+" ").indexOf(b)>-1},ATTR:function(a,b){var c=b[1],d=l.attrHandle[c]?l.attrHandle[c](a):a[c]!=null?a[c]:a.getAttribute(c),e=d+"",f=b[2],g=b[4];return d==null?f==="!=":f==="="?e===g:f==="*="?e.indexOf(g)>=0:f==="~="?(" "+e+" ").indexOf(g)>=0:g?f==="!="?e!==g:f==="^="?e.indexOf(g)===0:f==="$="?e.substr(e.length-g.length)===g:f==="|="?e===g||e.substr(0,g.length+1)===g+"-":!1:e&&d!==!1},POS:function(a,b,c,d){var e=b[2],f=l.setFilters[e];if(f)return f(a,c,b,d)}}},m=l.match.POS,n=function(a,b){return"\\"+(b-0+1)};for(var o in l.match)l.match[o]=new RegExp(l.match[o].source+/(?![^\[]*\])(?![^\(]*\))/.source),l.leftMatch[o]=new RegExp(/(^(?:.|\r|\n)*?)/.source+l.match[o].source.replace(/\\(\d+)/g,n));var p=function(a,b){a=Array.prototype.slice.call(a,0);if(b){b.push.apply(b,a);return b}return a};try{Array.prototype.slice.call(c.documentElement.childNodes,0)[0].nodeType}catch(q){p=function(a,b){var c=0,d=b||[];if(e.call(a)==="[object Array]")Array.prototype.push.apply(d,a);else if(typeof a.length=="number")for(var f=a.length;c<f;c++)d.push(a[c]);else for(;a[c];c++)d.push(a[c]);return d}}var r,s;c.documentElement.compareDocumentPosition?r=function(a,b){if(a===b){g=!0;return 0}if(!a.compareDocumentPosition||!b.compareDocumentPosition)return a.compareDocumentPosition?-1:1;return a.compareDocumentPosition(b)&4?-1:1}:(r=function(a,b){if(a===b){g=!0;return 0}if(a.sourceIndex&&b.sourceIndex)return a.sourceIndex-b.sourceIndex;var c,d,e=[],f=[],h=a.parentNode,i=b.parentNode,j=h;if(h===i)return s(a,b);if(!h)return-1;if(!i)return 1;while(j)e.unshift(j),j=j.parentNode;j=i;while(j)f.unshift(j),j=j.parentNode;c=e.length,d=f.length;for(var k=0;k<c&&k<d;k++)if(e[k]!==f[k])return s(e[k],f[k]);return k===c?s(a,f[k],-1):s(e[k],b,1)},s=function(a,b,c){if(a===b)return c;var d=a.nextSibling;while(d){if(d===b)return-1;d=d.nextSibling}return 1}),k.getText=function(a){var b="",c;for(var d=0;a[d];d++)c=a[d],c.nodeType===3||c.nodeType===4?b+=c.nodeValue:c.nodeType!==8&&(b+=k.getText(c.childNodes));return b},function(){var a=c.createElement("div"),d="script"+(new Date).getTime(),e=c.documentElement;a.innerHTML="<a name='"+d+"'/>",e.insertBefore(a,e.firstChild),c.getElementById(d)&&(l.find.ID=function(a,c,d){if(typeof c.getElementById!="undefined"&&!d){var e=c.getElementById(a[1]);return e?e.id===a[1]||typeof e.getAttributeNode!="undefined"&&e.getAttributeNode("id").nodeValue===a[1]?[e]:b:[]}},l.filter.ID=function(a,b){var c=typeof a.getAttributeNode!="undefined"&&a.getAttributeNode("id");return a.nodeType===1&&c&&c.nodeValue===b}),e.removeChild(a),e=a=null}(),function(){var a=c.createElement("div");a.appendChild(c.createComment("")),a.getElementsByTagName("*").length>0&&(l.find.TAG=function(a,b){var c=b.getElementsByTagName(a[1]);if(a[1]==="*"){var d=[];for(var e=0;c[e];e++)c[e].nodeType===1&&d.push(c[e]);c=d}return c}),a.innerHTML="<a href='#'></a>",a.firstChild&&typeof a.firstChild.getAttribute!="undefined"&&a.firstChild.getAttribute("href")!=="#"&&(l.attrHandle.href=function(a){return a.getAttribute("href",2)}),a=null}(),c.querySelectorAll&&function(){var a=k,b=c.createElement("div"),d="__sizzle__";b.innerHTML="<p class='TEST'></p>";if(!b.querySelectorAll||b.querySelectorAll(".TEST").length!==0){k=function(b,e,f,g){e=e||c;if(!g&&!k.isXML(e)){var h=/^(\w+$)|^\.([\w\-]+$)|^#([\w\-]+$)/.exec(b);if(h&&(e.nodeType===1||e.nodeType===9)){if(h[1])return p(e.getElementsByTagName(b),f);if(h[2]&&l.find.CLASS&&e.getElementsByClassName)return p(e.getElementsByClassName(h[2]),f)}if(e.nodeType===9){if(b==="body"&&e.body)return p([e.body],f);if(h&&h[3]){var i=e.getElementById(h[3]);if(!i||!i.parentNode)return p([],f);if(i.id===h[3])return p([i],f)}try{return p(e.querySelectorAll(b),f)}catch(j){}}else if(e.nodeType===1&&e.nodeName.toLowerCase()!=="object"){var m=e,n=e.getAttribute("id"),o=n||d,q=e.parentNode,r=/^\s*[+~]/.test(b);n?o=o.replace(/'/g,"\\$&"):e.setAttribute("id",o),r&&q&&(e=e.parentNode);try{if(!r||q)return p(e.querySelectorAll("[id='"+o+"'] "+b),f)}catch(s){}finally{n||m.removeAttribute("id")}}}return a(b,e,f,g)};for(var e in a)k[e]=a[e];b=null}}(),function(){var a=c.documentElement,b=a.matchesSelector||a.mozMatchesSelector||a.webkitMatchesSelector||a.msMatchesSelector;if(b){var d=!b.call(c.createElement("div"),"div"),e=!1;try{b.call(c.documentElement,"[test!='']:sizzle")}catch(f){e=!0}k.matchesSelector=function(a,c){c=c.replace(/\=\s*([^'"\]]*)\s*\]/g,"='$1']");if(!k.isXML(a))try{if(e||!l.match.PSEUDO.test(c)&&!/!=/.test(c)){var f=b.call(a,c);if(f||!d||a.document&&a.document.nodeType!==11)return f}}catch(g){}return k(c,null,null,[a]).length>0}}}(),function(){var a=c.createElement("div");a.innerHTML="<div class='test e'></div><div class='test'></div>";if(!!a.getElementsByClassName&&a.getElementsByClassName("e").length!==0){a.lastChild.className="e";if(a.getElementsByClassName("e").length===1)return;l.order.splice(1,0,"CLASS"),l.find.CLASS=function(a,b,c){if(typeof b.getElementsByClassName!="undefined"&&!c)return b.getElementsByClassName(a[1])},a=null}}(),c.documentElement.contains?k.contains=function(a,b){return a!==b&&(a.contains?a.contains(b):!0)}:c.documentElement.compareDocumentPosition?k.contains=function(a,b){return!!(a.compareDocumentPosition(b)&16)}:k.contains=function(){return!1},k.isXML=function(a){var b=(a?a.ownerDocument||a:0).documentElement;return b?b.nodeName!=="HTML":!1};var v=function(a,b){var c,d=[],e="",f=b.nodeType?[b]:b;while(c=l.match.PSEUDO.exec(a))e+=c[0],a=a.replace(l.match.PSEUDO,"");a=l.relative[a]?a+"*":a;for(var g=0,h=f.length;g<h;g++)k(a,f[g],d);return k.filter(e,d)};f.find=k,f.expr=k.selectors,f.expr[":"]=f.expr.filters,f.unique=k.uniqueSort,f.text=k.getText,f.isXMLDoc=k.isXML,f.contains=k.contains}();var O=/Until$/,P=/^(?:parents|prevUntil|prevAll)/,Q=/,/,R=/^.[^:#\[\.,]*$/,S=Array.prototype.slice,T=f.expr.match.POS,U={children:!0,contents:!0,next:!0,prev:!0};f.fn.extend({find:function(a){var b=this,c,d;if(typeof a!="string")return f(a).filter(function(){for(c=0,d=b.length;c<d;c++)if(f.contains(b[c],this))return!0});var e=this.pushStack("","find",a),g,h,i;for(c=0,d=this.length;c<d;c++){g=e.length,f.find(a,this[c],e);if(c>0)for(h=g;h<e.length;h++)for(i=0;i<g;i++)if(e[i]===e[h]){e.splice(h--,1);break}}return e},has:function(a){var b=f(a);return this.filter(function(){for(var a=0,c=b.length;a<c;a++)if(f.contains(this,b[a]))return!0})},not:function(a){return this.pushStack(W(this,a,!1),"not",a)},filter:function(a){return this.pushStack(W(this,a,!0),"filter",a)},is:function(a){return!!a&&(typeof a=="string"?f.filter(a,this).length>0:this.filter(a).length>0)},closest:function(a,b){var c=[],d,e,g=this[0];if(f.isArray(a)){var h,i,j={},k=1;if(g&&a.length){for(d=0,e=a.length;d<e;d++)i=a[d],j[i]||(j[i]=T.test(i)?f(i,b||this.context):i);while(g&&g.ownerDocument&&g!==b){for(i in j)h=j[i],(h.jquery?h.index(g)>-1:f(g).is(h))&&c.push({selector:i,elem:g,level:k});g=g.parentNode,k++}}return c}var l=T.test(a)||typeof a!="string"?f(a,b||this.context):0;for(d=0,e=this.length;d<e;d++){g=this[d];while(g){if(l?l.index(g)>-1:f.find.matchesSelector(g,a)){c.push(g);break}g=g.parentNode;if(!g||!g.ownerDocument||g===b||g.nodeType===11)break}}c=c.length>1?f.unique(c):c;return this.pushStack(c,"closest",a)},index:function(a){if(!a||typeof a=="string")return f.inArray(this[0],a?f(a):this.parent().children());return f.inArray(a.jquery?a[0]:a,this)},add:function(a,b){var c=typeof a=="string"?f(a,b):f.makeArray(a&&a.nodeType?[a]:a),d=f.merge(this.get(),c);return this.pushStack(V(c[0])||V(d[0])?d:f.unique(d))},andSelf:function(){return this.add(this.prevObject)}}),f.each({parent:function(a){var b=a.parentNode;return b&&b.nodeType!==11?b:null},parents:function(a){return f.dir(a,"parentNode")},parentsUntil:function(a,b,c){return f.dir(a,"parentNode",c)},next:function(a){return f.nth(a,2,"nextSibling")},prev:function(a){return f.nth(a,2,"previousSibling")},nextAll:function(a){return f.dir(a,"nextSibling")},prevAll:function(a){return f.dir(a,"previousSibling")},nextUntil:function(a,b,c){return f.dir(a,"nextSibling",c)},prevUntil:function(a,b,c){return f.dir(a,"previousSibling",c)},siblings:function(a){return f.sibling(a.parentNode.firstChild,a)},children:function(a){return f.sibling(a.firstChild)},contents:function(a){return f.nodeName(a,"iframe")?a.contentDocument||a.contentWindow.document:f.makeArray(a.childNodes)}},function(a,b){f.fn[a]=function(c,d){var e=f.map(this,b,c),g=S.call(arguments);O.test(a)||(d=c),d&&typeof d=="string"&&(e=f.filter(d,e)),e=this.length>1&&!U[a]?f.unique(e):e,(this.length>1||Q.test(d))&&P.test(a)&&(e=e.reverse());return this.pushStack(e,a,g.join(","))}}),f.extend({filter:function(a,b,c){c&&(a=":not("+a+")");return b.length===1?f.find.matchesSelector(b[0],a)?[b[0]]:[]:f.find.matches(a,b)},dir:function(a,c,d){var e=[],g=a[c];while(g&&g.nodeType!==9&&(d===b||g.nodeType!==1||!f(g).is(d)))g.nodeType===1&&e.push(g),g=g[c];return e},nth:function(a,b,c,d){b=b||1;var e=0;for(;a;a=a[c])if(a.nodeType===1&&++e===b)break;return a},sibling:function(a,b){var c=[];for(;a;a=a.nextSibling)a.nodeType===1&&a!==b&&c.push(a);return c}});var X=/ jQuery\d+="(?:\d+|null)"/g,Y=/^\s+/,Z=/<(?!area|br|col|embed|hr|img|input|link|meta|param)(([\w:]+)[^>]*)\/>/ig,$=/<([\w:]+)/,_=/<tbody/i,ba=/<|&#?\w+;/,bb=/<(?:script|object|embed|option|style)/i,bc=/checked\s*(?:[^=]|=\s*.checked.)/i,bd=/\/(java|ecma)script/i,be=/^\s*<!(?:\[CDATA\[|\-\-)/,bf={option:[1,"<select multiple='multiple'>","</select>"],legend:[1,"<fieldset>","</fieldset>"],thead:[1,"<table>","</table>"],tr:[2,"<table><tbody>","</tbody></table>"],td:[3,"<table><tbody><tr>","</tr></tbody></table>"],col:[2,"<table><tbody></tbody><colgroup>","</colgroup></table>"],area:[1,"<map>","</map>"],_default:[0,"",""]};bf.optgroup=bf.option,bf.tbody=bf.tfoot=bf.colgroup=bf.caption=bf.thead,bf.th=bf.td,f.support.htmlSerialize||(bf._default=[1,"div<div>","</div>"]),f.fn.extend({text:function(a){if(f.isFunction(a))return this.each(function(b){var c=f(this);c.text(a.call(this,b,c.text()))});if(typeof a!="object"&&a!==b)return this.empty().append((this[0]&&this[0].ownerDocument||c).createTextNode(a));return f.text(this)},wrapAll:function(a){if(f.isFunction(a))return this.each(function(b){f(this).wrapAll(a.call(this,b))});if(this[0]){var b=f(a,this[0].ownerDocument).eq(0).clone(!0);this[0].parentNode&&b.insertBefore(this[0]),b.map(function(){var a=this;while(a.firstChild&&a.firstChild.nodeType===1)a=a.firstChild;return a}).append(this)}return this},wrapInner:function(a){if(f.isFunction(a))return this.each(function(b){f(this).wrapInner(a.call(this,b))});return this.each(function(){var b=f(this),c=b.contents();c.length?c.wrapAll(a):b.append(a)})},wrap:function(a){return this.each(function(){f(this).wrapAll(a)})},unwrap:function(){return this.parent().each(function(){f.nodeName(this,"body")||f(this).replaceWith(this.childNodes)}).end()},append:function(){return this.domManip(arguments,!0,function(a){this.nodeType===1&&this.appendChild(a)})},prepend:function(){return this.domManip(arguments,!0,function(a){this.nodeType===1&&this.insertBefore(a,this.firstChild)})},before:function(){if(this[0]&&this[0].parentNode)return this.domManip(arguments,!1,function(a){this.parentNode.insertBefore(a,this)});if(arguments.length){var a=f(arguments[0]);a.push.apply(a,this.toArray());return this.pushStack(a,"before",arguments)}},after:function(){if(this[0]&&this[0].parentNode)return this.domManip(arguments,!1,function(a){this.parentNode.insertBefore(a,this.nextSibling)});if(arguments.length){var a=this.pushStack(this,"after",arguments);a.push.apply(a,f(arguments[0]).toArray());return a}},remove:function(a,b){for(var c=0,d;(d=this[c])!=null;c++)if(!a||f.filter(a,[d]).length)!b&&d.nodeType===1&&(f.cleanData(d.getElementsByTagName("*")),f.cleanData([d])),d.parentNode&&d.parentNode.removeChild(d);return this},empty:function(){for(var a=0,b;(b=this[a])!=null;a++){b.nodeType===1&&f.cleanData(b.getElementsByTagName("*"));while(b.firstChild)b.removeChild(b.firstChild)}return this},clone:function(a,b){a=a==null?!1:a,b=b==null?a:b;return this.map(function(){return f.clone(this,a,b)})},html:function(a){if(a===b)return this[0]&&this[0].nodeType===1?this[0].innerHTML.replace(X,""):null;if(typeof a=="string"&&!bb.test(a)&&(f.support.leadingWhitespace||!Y.test(a))&&!bf[($.exec(a)||["",""])[1].toLowerCase()]){a=a.replace(Z,"<$1></$2>");try{for(var c=0,d=this.length;c<d;c++)this[c].nodeType===1&&(f.cleanData(this[c].getElementsByTagName("*")),this[c].innerHTML=a)}catch(e){this.empty().append(a)}}else f.isFunction(a)?this.each(function(b){var c=f(this);c.html(a.call(this,b,c.html()))}):this.empty().append(a);return this},replaceWith:function(a){if(this[0]&&this[0].parentNode){if(f.isFunction(a))return this.each(function(b){var c=f(this),d=c.html();c.replaceWith(a.call(this,b,d))});typeof a!="string"&&(a=f(a).detach());return this.each(function(){var b=this.nextSibling,c=this.parentNode;f(this).remove(),b?f(b).before(a):f(c).append(a)})}return this.length?this.pushStack(f(f.isFunction(a)?a():a),"replaceWith",a):this},detach:function(a){return this.remove(a,!0)},domManip:function(a,c,d){var e,g,h,i,j=a[0],k=[];if(!f.support.checkClone&&arguments.length===3&&typeof j=="string"&&bc.test(j))return this.each(function(){f(this).domManip(a,c,d,!0)});if(f.isFunction(j))return this.each(function(e){var g=f(this);a[0]=j.call(this,e,c?g.html():b),g.domManip(a,c,d)});if(this[0]){i=j&&j.parentNode,f.support.parentNode&&i&&i.nodeType===11&&i.childNodes.length===this.length?e={fragment:i}:e=f.buildFragment(a,this,k),h=e.fragment,h.childNodes.length===1?g=h=h.firstChild:g=h.firstChild;if(g){c=c&&f.nodeName(g,"tr");for(var l=0,m=this.length,n=m-1;l<m;l++)d.call(c?bg(this[l],g):this[l],e.cacheable||m>1&&l<n?f.clone(h,!0,!0):h)}k.length&&f.each(k,bm)}return this}}),f.buildFragment=function(a,b,d){var e,g,h,i;b&&b[0]&&(i=b[0].ownerDocument||b[0]),i.createDocumentFragment||(i=c),a.length===1&&typeof a[0]=="string"&&a[0].length<512&&i===c&&a[0].charAt(0)==="<"&&!bb.test(a[0])&&(f.support.checkClone||!bc.test(a[0]))&&(g=!0,h=f.fragments[a[0]],h&&h!==1&&(e=h)),e||(e=i.createDocumentFragment(),f.clean(a,i,e,d)),g&&(f.fragments[a[0]]=h?e:1);return{fragment:e,cacheable:g}},f.fragments={},f.each({appendTo:"append",prependTo:"prepend",insertBefore:"before",insertAfter:"after",replaceAll:"replaceWith"},function(a,b){f.fn[a]=function(c){var d=[],e=f(c),g=this.length===1&&this[0].parentNode;if(g&&g.nodeType===11&&g.childNodes.length===1&&e.length===1){e[b](this[0]);return this}for(var h=0,i=e.length;h<i;h++){var j=(h>0?this.clone(!0):this).get();f(e[h])[b](j),d=d.concat(j
)}return this.pushStack(d,a,e.selector)}}),f.extend({clone:function(a,b,c){var d=a.cloneNode(!0),e,g,h;if((!f.support.noCloneEvent||!f.support.noCloneChecked)&&(a.nodeType===1||a.nodeType===11)&&!f.isXMLDoc(a)){bi(a,d),e=bj(a),g=bj(d);for(h=0;e[h];++h)bi(e[h],g[h])}if(b){bh(a,d);if(c){e=bj(a),g=bj(d);for(h=0;e[h];++h)bh(e[h],g[h])}}e=g=null;return d},clean:function(a,b,d,e){var g;b=b||c,typeof b.createElement=="undefined"&&(b=b.ownerDocument||b[0]&&b[0].ownerDocument||c);var h=[],i;for(var j=0,k;(k=a[j])!=null;j++){typeof k=="number"&&(k+="");if(!k)continue;if(typeof k=="string")if(!ba.test(k))k=b.createTextNode(k);else{k=k.replace(Z,"<$1></$2>");var l=($.exec(k)||["",""])[1].toLowerCase(),m=bf[l]||bf._default,n=m[0],o=b.createElement("div");o.innerHTML=m[1]+k+m[2];while(n--)o=o.lastChild;if(!f.support.tbody){var p=_.test(k),q=l==="table"&&!p?o.firstChild&&o.firstChild.childNodes:m[1]==="<table>"&&!p?o.childNodes:[];for(i=q.length-1;i>=0;--i)f.nodeName(q[i],"tbody")&&!q[i].childNodes.length&&q[i].parentNode.removeChild(q[i])}!f.support.leadingWhitespace&&Y.test(k)&&o.insertBefore(b.createTextNode(Y.exec(k)[0]),o.firstChild),k=o.childNodes}var r;if(!f.support.appendChecked)if(k[0]&&typeof (r=k.length)=="number")for(i=0;i<r;i++)bl(k[i]);else bl(k);k.nodeType?h.push(k):h=f.merge(h,k)}if(d){g=function(a){return!a.type||bd.test(a.type)};for(j=0;h[j];j++)if(e&&f.nodeName(h[j],"script")&&(!h[j].type||h[j].type.toLowerCase()==="text/javascript"))e.push(h[j].parentNode?h[j].parentNode.removeChild(h[j]):h[j]);else{if(h[j].nodeType===1){var s=f.grep(h[j].getElementsByTagName("script"),g);h.splice.apply(h,[j+1,0].concat(s))}d.appendChild(h[j])}}return h},cleanData:function(a){var b,c,d=f.cache,e=f.expando,g=f.event.special,h=f.support.deleteExpando;for(var i=0,j;(j=a[i])!=null;i++){if(j.nodeName&&f.noData[j.nodeName.toLowerCase()])continue;c=j[f.expando];if(c){b=d[c]&&d[c][e];if(b&&b.events){for(var k in b.events)g[k]?f.event.remove(j,k):f.removeEvent(j,k,b.handle);b.handle&&(b.handle.elem=null)}h?delete j[f.expando]:j.removeAttribute&&j.removeAttribute(f.expando),delete d[c]}}}});var bn=/alpha\([^)]*\)/i,bo=/opacity=([^)]*)/,bp=/([A-Z]|^ms)/g,bq=/^-?\d+(?:px)?$/i,br=/^-?\d/,bs=/^[+\-]=/,bt=/[^+\-\.\de]+/g,bu={position:"absolute",visibility:"hidden",display:"block"},bv=["Left","Right"],bw=["Top","Bottom"],bx,by,bz;f.fn.css=function(a,c){if(arguments.length===2&&c===b)return this;return f.access(this,a,c,!0,function(a,c,d){return d!==b?f.style(a,c,d):f.css(a,c)})},f.extend({cssHooks:{opacity:{get:function(a,b){if(b){var c=bx(a,"opacity","opacity");return c===""?"1":c}return a.style.opacity}}},cssNumber:{fillOpacity:!0,fontWeight:!0,lineHeight:!0,opacity:!0,orphans:!0,widows:!0,zIndex:!0,zoom:!0},cssProps:{"float":f.support.cssFloat?"cssFloat":"styleFloat"},style:function(a,c,d,e){if(!!a&&a.nodeType!==3&&a.nodeType!==8&&!!a.style){var g,h,i=f.camelCase(c),j=a.style,k=f.cssHooks[i];c=f.cssProps[i]||i;if(d===b){if(k&&"get"in k&&(g=k.get(a,!1,e))!==b)return g;return j[c]}h=typeof d;if(h==="number"&&isNaN(d)||d==null)return;h==="string"&&bs.test(d)&&(d=+d.replace(bt,"")+parseFloat(f.css(a,c)),h="number"),h==="number"&&!f.cssNumber[i]&&(d+="px");if(!k||!("set"in k)||(d=k.set(a,d))!==b)try{j[c]=d}catch(l){}}},css:function(a,c,d){var e,g;c=f.camelCase(c),g=f.cssHooks[c],c=f.cssProps[c]||c,c==="cssFloat"&&(c="float");if(g&&"get"in g&&(e=g.get(a,!0,d))!==b)return e;if(bx)return bx(a,c)},swap:function(a,b,c){var d={};for(var e in b)d[e]=a.style[e],a.style[e]=b[e];c.call(a);for(e in b)a.style[e]=d[e]}}),f.curCSS=f.css,f.each(["height","width"],function(a,b){f.cssHooks[b]={get:function(a,c,d){var e;if(c){if(a.offsetWidth!==0)return bA(a,b,d);f.swap(a,bu,function(){e=bA(a,b,d)});return e}},set:function(a,b){if(!bq.test(b))return b;b=parseFloat(b);if(b>=0)return b+"px"}}}),f.support.opacity||(f.cssHooks.opacity={get:function(a,b){return bo.test((b&&a.currentStyle?a.currentStyle.filter:a.style.filter)||"")?parseFloat(RegExp.$1)/100+"":b?"1":""},set:function(a,b){var c=a.style,d=a.currentStyle;c.zoom=1;var e=f.isNaN(b)?"":"alpha(opacity="+b*100+")",g=d&&d.filter||c.filter||"";c.filter=bn.test(g)?g.replace(bn,e):g+" "+e}}),f(function(){f.support.reliableMarginRight||(f.cssHooks.marginRight={get:function(a,b){var c;f.swap(a,{display:"inline-block"},function(){b?c=bx(a,"margin-right","marginRight"):c=a.style.marginRight});return c}})}),c.defaultView&&c.defaultView.getComputedStyle&&(by=function(a,c){var d,e,g;c=c.replace(bp,"-$1").toLowerCase();if(!(e=a.ownerDocument.defaultView))return b;if(g=e.getComputedStyle(a,null))d=g.getPropertyValue(c),d===""&&!f.contains(a.ownerDocument.documentElement,a)&&(d=f.style(a,c));return d}),c.documentElement.currentStyle&&(bz=function(a,b){var c,d=a.currentStyle&&a.currentStyle[b],e=a.runtimeStyle&&a.runtimeStyle[b],f=a.style;!bq.test(d)&&br.test(d)&&(c=f.left,e&&(a.runtimeStyle.left=a.currentStyle.left),f.left=b==="fontSize"?"1em":d||0,d=f.pixelLeft+"px",f.left=c,e&&(a.runtimeStyle.left=e));return d===""?"auto":d}),bx=by||bz,f.expr&&f.expr.filters&&(f.expr.filters.hidden=function(a){var b=a.offsetWidth,c=a.offsetHeight;return b===0&&c===0||!f.support.reliableHiddenOffsets&&(a.style.display||f.css(a,"display"))==="none"},f.expr.filters.visible=function(a){return!f.expr.filters.hidden(a)});var bB=/%20/g,bC=/\[\]$/,bD=/\r?\n/g,bE=/#.*$/,bF=/^(.*?):[ \t]*([^\r\n]*)\r?$/mg,bG=/^(?:color|date|datetime|email|hidden|month|number|password|range|search|tel|text|time|url|week)$/i,bH=/^(?:about|app|app\-storage|.+\-extension|file|widget):$/,bI=/^(?:GET|HEAD)$/,bJ=/^\/\//,bK=/\?/,bL=/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,bM=/^(?:select|textarea)/i,bN=/\s+/,bO=/([?&])_=[^&]*/,bP=/^([\w\+\.\-]+:)(?:\/\/([^\/?#:]*)(?::(\d+))?)?/,bQ=f.fn.load,bR={},bS={},bT,bU;try{bT=e.href}catch(bV){bT=c.createElement("a"),bT.href="",bT=bT.href}bU=bP.exec(bT.toLowerCase())||[],f.fn.extend({load:function(a,c,d){if(typeof a!="string"&&bQ)return bQ.apply(this,arguments);if(!this.length)return this;var e=a.indexOf(" ");if(e>=0){var g=a.slice(e,a.length);a=a.slice(0,e)}var h="GET";c&&(f.isFunction(c)?(d=c,c=b):typeof c=="object"&&(c=f.param(c,f.ajaxSettings.traditional),h="POST"));var i=this;f.ajax({url:a,type:h,dataType:"html",data:c,complete:function(a,b,c){c=a.responseText,a.isResolved()&&(a.done(function(a){c=a}),i.html(g?f("<div>").append(c.replace(bL,"")).find(g):c)),d&&i.each(d,[c,b,a])}});return this},serialize:function(){return f.param(this.serializeArray())},serializeArray:function(){return this.map(function(){return this.elements?f.makeArray(this.elements):this}).filter(function(){return this.name&&!this.disabled&&(this.checked||bM.test(this.nodeName)||bG.test(this.type))}).map(function(a,b){var c=f(this).val();return c==null?null:f.isArray(c)?f.map(c,function(a,c){return{name:b.name,value:a.replace(bD,"\r\n")}}):{name:b.name,value:c.replace(bD,"\r\n")}}).get()}}),f.each("ajaxStart ajaxStop ajaxComplete ajaxError ajaxSuccess ajaxSend".split(" "),function(a,b){f.fn[b]=function(a){return this.bind(b,a)}}),f.each(["get","post"],function(a,c){f[c]=function(a,d,e,g){f.isFunction(d)&&(g=g||e,e=d,d=b);return f.ajax({type:c,url:a,data:d,success:e,dataType:g})}}),f.extend({getScript:function(a,c){return f.get(a,b,c,"script")},getJSON:function(a,b,c){return f.get(a,b,c,"json")},ajaxSetup:function(a,b){b?f.extend(!0,a,f.ajaxSettings,b):(b=a,a=f.extend(!0,f.ajaxSettings,b));for(var c in{context:1,url:1})c in b?a[c]=b[c]:c in f.ajaxSettings&&(a[c]=f.ajaxSettings[c]);return a},ajaxSettings:{url:bT,isLocal:bH.test(bU[1]),global:!0,type:"GET",contentType:"application/x-www-form-urlencoded",processData:!0,async:!0,accepts:{xml:"application/xml, text/xml",html:"text/html",text:"text/plain",json:"application/json, text/javascript","*":"*/*"},contents:{xml:/xml/,html:/html/,json:/json/},responseFields:{xml:"responseXML",text:"responseText"},converters:{"* text":a.String,"text html":!0,"text json":f.parseJSON,"text xml":f.parseXML}},ajaxPrefilter:bW(bR),ajaxTransport:bW(bS),ajax:function(a,c){function w(a,c,l,m){if(s!==2){s=2,q&&clearTimeout(q),p=b,n=m||"",v.readyState=a?4:0;var o,r,u,w=l?bZ(d,v,l):b,x,y;if(a>=200&&a<300||a===304){if(d.ifModified){if(x=v.getResponseHeader("Last-Modified"))f.lastModified[k]=x;if(y=v.getResponseHeader("Etag"))f.etag[k]=y}if(a===304)c="notmodified",o=!0;else try{r=b$(d,w),c="success",o=!0}catch(z){c="parsererror",u=z}}else{u=c;if(!c||a)c="error",a<0&&(a=0)}v.status=a,v.statusText=c,o?h.resolveWith(e,[r,c,v]):h.rejectWith(e,[v,c,u]),v.statusCode(j),j=b,t&&g.trigger("ajax"+(o?"Success":"Error"),[v,d,o?r:u]),i.resolveWith(e,[v,c]),t&&(g.trigger("ajaxComplete",[v,d]),--f.active||f.event.trigger("ajaxStop"))}}typeof a=="object"&&(c=a,a=b),c=c||{};var d=f.ajaxSetup({},c),e=d.context||d,g=e!==d&&(e.nodeType||e instanceof f)?f(e):f.event,h=f.Deferred(),i=f._Deferred(),j=d.statusCode||{},k,l={},m={},n,o,p,q,r,s=0,t,u,v={readyState:0,setRequestHeader:function(a,b){if(!s){var c=a.toLowerCase();a=m[c]=m[c]||a,l[a]=b}return this},getAllResponseHeaders:function(){return s===2?n:null},getResponseHeader:function(a){var c;if(s===2){if(!o){o={};while(c=bF.exec(n))o[c[1].toLowerCase()]=c[2]}c=o[a.toLowerCase()]}return c===b?null:c},overrideMimeType:function(a){s||(d.mimeType=a);return this},abort:function(a){a=a||"abort",p&&p.abort(a),w(0,a);return this}};h.promise(v),v.success=v.done,v.error=v.fail,v.complete=i.done,v.statusCode=function(a){if(a){var b;if(s<2)for(b in a)j[b]=[j[b],a[b]];else b=a[v.status],v.then(b,b)}return this},d.url=((a||d.url)+"").replace(bE,"").replace(bJ,bU[1]+"//"),d.dataTypes=f.trim(d.dataType||"*").toLowerCase().split(bN),d.crossDomain==null&&(r=bP.exec(d.url.toLowerCase()),d.crossDomain=!(!r||r[1]==bU[1]&&r[2]==bU[2]&&(r[3]||(r[1]==="http:"?80:443))==(bU[3]||(bU[1]==="http:"?80:443)))),d.data&&d.processData&&typeof d.data!="string"&&(d.data=f.param(d.data,d.traditional)),bX(bR,d,c,v);if(s===2)return!1;t=d.global,d.type=d.type.toUpperCase(),d.hasContent=!bI.test(d.type),t&&f.active++===0&&f.event.trigger("ajaxStart");if(!d.hasContent){d.data&&(d.url+=(bK.test(d.url)?"&":"?")+d.data),k=d.url;if(d.cache===!1){var x=f.now(),y=d.url.replace(bO,"$1_="+x);d.url=y+(y===d.url?(bK.test(d.url)?"&":"?")+"_="+x:"")}}(d.data&&d.hasContent&&d.contentType!==!1||c.contentType)&&v.setRequestHeader("Content-Type",d.contentType),d.ifModified&&(k=k||d.url,f.lastModified[k]&&v.setRequestHeader("If-Modified-Since",f.lastModified[k]),f.etag[k]&&v.setRequestHeader("If-None-Match",f.etag[k])),v.setRequestHeader("Accept",d.dataTypes[0]&&d.accepts[d.dataTypes[0]]?d.accepts[d.dataTypes[0]]+(d.dataTypes[0]!=="*"?", */*; q=0.01":""):d.accepts["*"]);for(u in d.headers)v.setRequestHeader(u,d.headers[u]);if(d.beforeSend&&(d.beforeSend.call(e,v,d)===!1||s===2)){v.abort();return!1}for(u in{success:1,error:1,complete:1})v[u](d[u]);p=bX(bS,d,c,v);if(!p)w(-1,"No Transport");else{v.readyState=1,t&&g.trigger("ajaxSend",[v,d]),d.async&&d.timeout>0&&(q=setTimeout(function(){v.abort("timeout")},d.timeout));try{s=1,p.send(l,w)}catch(z){status<2?w(-1,z):f.error(z)}}return v},param:function(a,c){var d=[],e=function(a,b){b=f.isFunction(b)?b():b,d[d.length]=encodeURIComponent(a)+"="+encodeURIComponent(b)};c===b&&(c=f.ajaxSettings.traditional);if(f.isArray(a)||a.jquery&&!f.isPlainObject(a))f.each(a,function(){e(this.name,this.value)});else for(var g in a)bY(g,a[g],c,e);return d.join("&").replace(bB,"+")}}),f.extend({active:0,lastModified:{},etag:{}});var b_=f.now(),ca=/(\=)\?(&|$)|\?\?/i;f.ajaxSetup({jsonp:"callback",jsonpCallback:function(){return f.expando+"_"+b_++}}),f.ajaxPrefilter("json jsonp",function(b,c,d){var e=b.contentType==="application/x-www-form-urlencoded"&&typeof b.data=="string";if(b.dataTypes[0]==="jsonp"||b.jsonp!==!1&&(ca.test(b.url)||e&&ca.test(b.data))){var g,h=b.jsonpCallback=f.isFunction(b.jsonpCallback)?b.jsonpCallback():b.jsonpCallback,i=a[h],j=b.url,k=b.data,l="$1"+h+"$2";b.jsonp!==!1&&(j=j.replace(ca,l),b.url===j&&(e&&(k=k.replace(ca,l)),b.data===k&&(j+=(/\?/.test(j)?"&":"?")+b.jsonp+"="+h))),b.url=j,b.data=k,a[h]=function(a){g=[a]},d.always(function(){a[h]=i,g&&f.isFunction(i)&&a[h](g[0])}),b.converters["script json"]=function(){g||f.error(h+" was not called");return g[0]},b.dataTypes[0]="json";return"script"}}),f.ajaxSetup({accepts:{script:"text/javascript, application/javascript, application/ecmascript, application/x-ecmascript"},contents:{script:/javascript|ecmascript/},converters:{"text script":function(a){f.globalEval(a);return a}}}),f.ajaxPrefilter("script",function(a){a.cache===b&&(a.cache=!1),a.crossDomain&&(a.type="GET",a.global=!1)}),f.ajaxTransport("script",function(a){if(a.crossDomain){var d,e=c.head||c.getElementsByTagName("head")[0]||c.documentElement;return{send:function(f,g){d=c.createElement("script"),d.async="async",a.scriptCharset&&(d.charset=a.scriptCharset),d.src=a.url,d.onload=d.onreadystatechange=function(a,c){if(c||!d.readyState||/loaded|complete/.test(d.readyState))d.onload=d.onreadystatechange=null,e&&d.parentNode&&e.removeChild(d),d=b,c||g(200,"success")},e.insertBefore(d,e.firstChild)},abort:function(){d&&d.onload(0,1)}}}});var cb=a.ActiveXObject?function(){for(var a in cd)cd[a](0,1)}:!1,cc=0,cd;f.ajaxSettings.xhr=a.ActiveXObject?function(){return!this.isLocal&&ce()||cf()}:ce,function(a){f.extend(f.support,{ajax:!!a,cors:!!a&&"withCredentials"in a})}(f.ajaxSettings.xhr()),f.support.ajax&&f.ajaxTransport(function(c){if(!c.crossDomain||f.support.cors){var d;return{send:function(e,g){var h=c.xhr(),i,j;c.username?h.open(c.type,c.url,c.async,c.username,c.password):h.open(c.type,c.url,c.async);if(c.xhrFields)for(j in c.xhrFields)h[j]=c.xhrFields[j];c.mimeType&&h.overrideMimeType&&h.overrideMimeType(c.mimeType),!c.crossDomain&&!e["X-Requested-With"]&&(e["X-Requested-With"]="XMLHttpRequest");try{for(j in e)h.setRequestHeader(j,e[j])}catch(k){}h.send(c.hasContent&&c.data||null),d=function(a,e){var j,k,l,m,n;try{if(d&&(e||h.readyState===4)){d=b,i&&(h.onreadystatechange=f.noop,cb&&delete cd[i]);if(e)h.readyState!==4&&h.abort();else{j=h.status,l=h.getAllResponseHeaders(),m={},n=h.responseXML,n&&n.documentElement&&(m.xml=n),m.text=h.responseText;try{k=h.statusText}catch(o){k=""}!j&&c.isLocal&&!c.crossDomain?j=m.text?200:404:j===1223&&(j=204)}}}catch(p){e||g(-1,p)}m&&g(j,k,m,l)},!c.async||h.readyState===4?d():(i=++cc,cb&&(cd||(cd={},f(a).unload(cb)),cd[i]=d),h.onreadystatechange=d)},abort:function(){d&&d(0,1)}}}});var cg={},ch,ci,cj=/^(?:toggle|show|hide)$/,ck=/^([+\-]=)?([\d+.\-]+)([a-z%]*)$/i,cl,cm=[["height","marginTop","marginBottom","paddingTop","paddingBottom"],["width","marginLeft","marginRight","paddingLeft","paddingRight"],["opacity"]],cn,co=a.webkitRequestAnimationFrame||a.mozRequestAnimationFrame||a.oRequestAnimationFrame;f.fn.extend({show:function(a,b,c){var d,e;if(a||a===0)return this.animate(cr("show",3),a,b,c);for(var g=0,h=this.length;g<h;g++)d=this[g],d.style&&(e=d.style.display,!f._data(d,"olddisplay")&&e==="none"&&(e=d.style.display=""),e===""&&f.css(d,"display")==="none"&&f._data(d,"olddisplay",cs(d.nodeName)));for(g=0;g<h;g++){d=this[g];if(d.style){e=d.style.display;if(e===""||e==="none")d.style.display=f._data(d,"olddisplay")||""}}return this},hide:function(a,b,c){if(a||a===0)return this.animate(cr("hide",3),a,b,c);for(var d=0,e=this.length;d<e;d++)if(this[d].style){var g=f.css(this[d],"display");g!=="none"&&!f._data(this[d],"olddisplay")&&f._data(this[d],"olddisplay",g)}for(d=0;d<e;d++)this[d].style&&(this[d].style.display="none");return this},_toggle:f.fn.toggle,toggle:function(a,b,c){var d=typeof a=="boolean";f.isFunction(a)&&f.isFunction(b)?this._toggle.apply(this,arguments):a==null||d?this.each(function(){var b=d?a:f(this).is(":hidden");f(this)[b?"show":"hide"]()}):this.animate(cr("toggle",3),a,b,c);return this},fadeTo:function(a,b,c,d){return this.filter(":hidden").css("opacity",0).show().end().animate({opacity:b},a,c,d)},animate:function(a,b,c,d){var e=f.speed(b,c,d);if(f.isEmptyObject(a))return this.each(e.complete,[!1]);a=f.extend({},a);return this[e.queue===!1?"each":"queue"](function(){e.queue===!1&&f._mark(this);var b=f.extend({},e),c=this.nodeType===1,d=c&&f(this).is(":hidden"),g,h,i,j,k,l,m,n,o;b.animatedProperties={};for(i in a){g=f.camelCase(i),i!==g&&(a[g]=a[i],delete a[i]),h=a[g],f.isArray(h)?(b.animatedProperties[g]=h[1],h=a[g]=h[0]):b.animatedProperties[g]=b.specialEasing&&b.specialEasing[g]||b.easing||"swing";if(h==="hide"&&d||h==="show"&&!d)return b.complete.call(this);c&&(g==="height"||g==="width")&&(b.overflow=[this.style.overflow,this.style.overflowX,this.style.overflowY],f.css(this,"display")==="inline"&&f.css(this,"float")==="none"&&(f.support.inlineBlockNeedsLayout?(j=cs(this.nodeName),j==="inline"?this.style.display="inline-block":(this.style.display="inline",this.style.zoom=1)):this.style.display="inline-block"))}b.overflow!=null&&(this.style.overflow="hidden");for(i in a)k=new f.fx(this,b,i),h=a[i],cj.test(h)?k[h==="toggle"?d?"show":"hide":h]():(l=ck.exec(h),m=k.cur(),l?(n=parseFloat(l[2]),o=l[3]||(f.cssNumber[i]?"":"px"),o!=="px"&&(f.style(this,i,(n||1)+o),m=(n||1)/k.cur()*m,f.style(this,i,m+o)),l[1]&&(n=(l[1]==="-="?-1:1)*n+m),k.custom(m,n,o)):k.custom(m,h,""));return!0})},stop:function(a,b){a&&this.queue([]),this.each(function(){var a=f.timers,c=a.length;b||f._unmark(!0,this);while(c--)a[c].elem===this&&(b&&a[c](!0),a.splice(c,1))}),b||this.dequeue();return this}}),f.each({slideDown:cr("show",1),slideUp:cr("hide",1),slideToggle:cr("toggle",1),fadeIn:{opacity:"show"},fadeOut:{opacity:"hide"},fadeToggle:{opacity:"toggle"}},function(a,b){f.fn[a]=function(a,c,d){return this.animate(b,a,c,d)}}),f.extend({speed:function(a,b,c){var d=a&&typeof a=="object"?f.extend({},a):{complete:c||!c&&b||f.isFunction(a)&&a,duration:a,easing:c&&b||b&&!f.isFunction(b)&&b};d.duration=f.fx.off?0:typeof d.duration=="number"?d.duration:d.duration in f.fx.speeds?f.fx.speeds[d.duration]:f.fx.speeds._default,d.old=d.complete,d.complete=function(a){f.isFunction(d.old)&&d.old.call(this),d.queue!==!1?f.dequeue(this):a!==!1&&f._unmark(this)};return d},easing:{linear:function(a,b,c,d){return c+d*a},swing:function(a,b,c,d){return(-Math.cos(a*Math.PI)/2+.5)*d+c}},timers:[],fx:function(a,b,c){this.options=b,this.elem=a,this.prop=c,b.orig=b.orig||{}}}),f.fx.prototype={update:function(){this.options.step&&this.options.step.call(this.elem,this.now,this),(f.fx.step[this.prop]||f.fx.step._default)(this)},cur:function(){if(this.elem[this.prop]!=null&&(!this.elem.style||this.elem.style[this.prop]==null))return this.elem[this.prop];var a,b=f.css(this.elem,this.prop);return isNaN(a=parseFloat(b))?!b||b==="auto"?0:b:a},custom:function(a,b,c){function h(a){return d.step(a)}var d=this,e=f.fx,g;this.startTime=cn||cp(),this.start=a,this.end=b,this.unit=c||this.unit||(f.cssNumber[this.prop]?"":"px"),this.now=this.start,this.pos=this.state=0,h.elem=this.elem,h()&&f.timers.push(h)&&!cl&&(co?(cl=!0,g=function(){cl&&(co(g),e.tick())},co(g)):cl=setInterval(e.tick,e.interval))},show:function(){this.options.orig[this.prop]=f.style(this.elem,this.prop),this.options.show=!0,this.custom(this.prop==="width"||this.prop==="height"?1:0,this.cur()),f(this.elem).show()},hide:function(){this.options.orig[this.prop]=f.style(this.elem,this.prop),this.options.hide=!0,this.custom(this.cur(),0)},step:function(a){var b=cn||cp(),c=!0,d=this.elem,e=this.options,g,h;if(a||b>=e.duration+this.startTime){this.now=this.end,this.pos=this.state=1,this.update(),e.animatedProperties[this.prop]=!0;for(g in e.animatedProperties)e.animatedProperties[g]!==!0&&(c=!1);if(c){e.overflow!=null&&!f.support.shrinkWrapBlocks&&f.each(["","X","Y"],function(a,b){d.style["overflow"+b]=e.overflow[a]}),e.hide&&f(d).hide();if(e.hide||e.show)for(var i in e.animatedProperties)f.style(d,i,e.orig[i]);e.complete.call(d)}return!1}e.duration==Infinity?this.now=b:(h=b-this.startTime,this.state=h/e.duration,this.pos=f.easing[e.animatedProperties[this.prop]](this.state,h,0,1,e.duration),this.now=this.start+(this.end-this.start)*this.pos),this.update();return!0}},f.extend(f.fx,{tick:function(){for(var a=f.timers,b=0;b<a.length;++b)a[b]()||a.splice(b--,1);a.length||f.fx.stop()},interval:13,stop:function(){clearInterval(cl),cl=null},speeds:{slow:600,fast:200,_default:400},step:{opacity:function(a){f.style(a.elem,"opacity",a.now)},_default:function(a){a.elem.style&&a.elem.style[a.prop]!=null?a.elem.style[a.prop]=(a.prop==="width"||a.prop==="height"?Math.max(0,a.now):a.now)+a.unit:a.elem[a.prop]=a.now}}}),f.expr&&f.expr.filters&&(f.expr.filters.animated=function(a){return f.grep(f.timers,function(b){return a===b.elem}).length});var ct=/^t(?:able|d|h)$/i,cu=/^(?:body|html)$/i;"getBoundingClientRect"in c.documentElement?f.fn.offset=function(a){var b=this[0],c;if(a)return this.each(function(b){f.offset.setOffset(this,a,b)});if(!b||!b.ownerDocument)return null;if(b===b.ownerDocument.body)return f.offset.bodyOffset(b);try{c=b.getBoundingClientRect()}catch(d){}var e=b.ownerDocument,g=e.documentElement;if(!c||!f.contains(g,b))return c?{top:c.top,left:c.left}:{top:0,left:0};var h=e.body,i=cv(e),j=g.clientTop||h.clientTop||0,k=g.clientLeft||h.clientLeft||0,l=i.pageYOffset||f.support.boxModel&&g.scrollTop||h.scrollTop,m=i.pageXOffset||f.support.boxModel&&g.scrollLeft||h.scrollLeft,n=c.top+l-j,o=c.left+m-k;return{top:n,left:o}}:f.fn.offset=function(a){var b=this[0];if(a)return this.each(function(b){f.offset.setOffset(this,a,b)});if(!b||!b.ownerDocument)return null;if(b===b.ownerDocument.body)return f.offset.bodyOffset(b);f.offset.initialize();var c,d=b.offsetParent,e=b,g=b.ownerDocument,h=g.documentElement,i=g.body,j=g.defaultView,k=j?j.getComputedStyle(b,null):b.currentStyle,l=b.offsetTop,m=b.offsetLeft;while((b=b.parentNode)&&b!==i&&b!==h){if(f.offset.supportsFixedPosition&&k.position==="fixed")break;c=j?j.getComputedStyle(b,null):b.currentStyle,l-=b.scrollTop,m-=b.scrollLeft,b===d&&(l+=b.offsetTop,m+=b.offsetLeft,f.offset.doesNotAddBorder&&(!f.offset.doesAddBorderForTableAndCells||!ct.test(b.nodeName))&&(l+=parseFloat(c.borderTopWidth)||0,m+=parseFloat(c.borderLeftWidth)||0),e=d,d=b.offsetParent),f.offset.subtractsBorderForOverflowNotVisible&&c.overflow!=="visible"&&(l+=parseFloat(c.borderTopWidth)||0,m+=parseFloat(c.borderLeftWidth)||0),k=c}if(k.position==="relative"||k.position==="static")l+=i.offsetTop,m+=i.offsetLeft;f.offset.supportsFixedPosition&&k.position==="fixed"&&(l+=Math.max(h.scrollTop,i.scrollTop),m+=Math.max(h.scrollLeft,i.scrollLeft));return{top:l,left:m}},f.offset={initialize:function(){var a=c.body,b=c.createElement("div"),d,e,g,h,i=parseFloat(f.css(a,"marginTop"))||0,j="<div style='position:absolute;top:0;left:0;margin:0;border:5px solid #000;padding:0;width:1px;height:1px;'><div></div></div><table style='position:absolute;top:0;left:0;margin:0;border:5px solid #000;padding:0;width:1px;height:1px;' cellpadding='0' cellspacing='0'><tr><td></td></tr></table>";f.extend(b.style,{position:"absolute",top:0,left:0,margin:0,border:0,width:"1px",height:"1px",visibility:"hidden"}),b.innerHTML=j,a.insertBefore(b,a.firstChild),d=b.firstChild,e=d.firstChild,h=d.nextSibling.firstChild.firstChild,this.doesNotAddBorder=e.offsetTop!==5,this.doesAddBorderForTableAndCells=h.offsetTop===5,e.style.position="fixed",e.style.top="20px",this.supportsFixedPosition=e.offsetTop===20||e.offsetTop===15,e.style.position=e.style.top="",d.style.overflow="hidden",d.style.position="relative",this.subtractsBorderForOverflowNotVisible=e.offsetTop===-5,this.doesNotIncludeMarginInBodyOffset=a.offsetTop!==i,a.removeChild(b),f.offset.initialize=f.noop},bodyOffset:function(a){var b=a.offsetTop,c=a.offsetLeft;f.offset.initialize(),f.offset.doesNotIncludeMarginInBodyOffset&&(b+=parseFloat(f.css(a,"marginTop"))||0,c+=parseFloat(f.css(a,"marginLeft"))||0);return{top:b,left:c}},setOffset:function(a,b,c){var d=f.css(a,"position");d==="static"&&(a.style.position="relative");var e=f(a),g=e.offset(),h=f.css(a,"top"),i=f.css(a,"left"),j=(d==="absolute"||d==="fixed")&&f.inArray("auto",[h,i])>-1,k={},l={},m,n;j?(l=e.position(),m=l.top,n=l.left):(m=parseFloat(h)||0,n=parseFloat(i)||0),f.isFunction(b)&&(b=b.call(a,c,g)),b.top!=null&&(k.top=b.top-g.top+m),b.left!=null&&(k.left=b.left-g.left+n),"using"in b?b.using.call(a,k):e.css(k)}},f.fn.extend({position:function(){if(!this[0])return null;var a=this[0],b=this.offsetParent(),c=this.offset(),d=cu.test(b[0].nodeName)?{top:0,left:0}:b.offset();c.top-=parseFloat(f.css(a,"marginTop"))||0,c.left-=parseFloat(f.css(a,"marginLeft"))||0,d.top+=parseFloat(f.css(b[0],"borderTopWidth"))||0,d.left+=parseFloat(f.css(b[0],"borderLeftWidth"))||0;return{top:c.top-d.top,left:c.left-d.left}},offsetParent:function(){return this.map(function(){var a=this.offsetParent||c.body;while(a&&!cu.test(a.nodeName)&&f.css(a,"position")==="static")a=a.offsetParent;return a})}}),f.each(["Left","Top"],function(a,c){var d="scroll"+c;f.fn[d]=function(c){var e,g;if(c===b){e=this[0];if(!e)return null;g=cv(e);return g?"pageXOffset"in g?g[a?"pageYOffset":"pageXOffset"]:f.support.boxModel&&g.document.documentElement[d]||g.document.body[d]:e[d]}return this.each(function(){g=cv(this),g?g.scrollTo(a?f(g).scrollLeft():c,a?c:f(g).scrollTop()):this[d]=c})}}),f.each(["Height","Width"],function(a,c){var d=c.toLowerCase();f.fn["inner"+c]=function(){var a=this[0];return a&&a.style?parseFloat(f.css(a,d,"padding")):null},f.fn["outer"+c]=function(a){var b=this[0];return b&&b.style?parseFloat(f.css(b,d,a?"margin":"border")):null},f.fn[d]=function(a){var e=this[0];if(!e)return a==null?null:this;if(f.isFunction(a))return this.each(function(b){var c=f(this);c[d](a.call(this,b,c[d]()))});if(f.isWindow(e)){var g=e.document.documentElement["client"+c];return e.document.compatMode==="CSS1Compat"&&g||e.document.body["client"+c]||g}if(e.nodeType===9)return Math.max(e.documentElement["client"+c],e.body["scroll"+c],e.documentElement["scroll"+c],e.body["offset"+c],e.documentElement["offset"+c]);if(a===b){var h=f.css(e,d),i=parseFloat(h);return f.isNaN(i)?h:i}return this.css(d,typeof a=="string"?a:a+"px")}}),a.jQuery=a.$=f})(window);



