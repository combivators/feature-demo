"use strict";
//Inner functions
function _(el) {return document.getElementById(el);} // Like jQuery $('#id')
function _c(c) {return document.querySelector(c);}
//删除重复数组
function _arrayUnique(a) {return a.filter(function (x, i, self) {return self.indexOf(x) === i;});}
//重复数组
function _arrayDuplicates(a) {return a.filter(function (x, i, self) {return self.indexOf(x) === i && i !== self.lastIndexOf(x);});}
//比较数组
function _arrayEqual(a, b) {
  if (!Array.isArray(a)) return false;
  if (!Array.isArray(b)) return false;
  if (a.length != b.length) return false;
  for (var i = 0, n = a.length; i < n; ++i) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}

function _cookies(){
  var cookies = new Array();
  if(document.cookie != ''){
    var tmp = document.cookie.split('; ');
    for(var i=0;i<tmp.length;i++){
      var data = tmp[i].split('=');
      cookies[data[0]] = decodeURIComponent(data[1]);
    }
  }
  return cookies;
}
function _cookie(key){return cookies()[key];}

//插件主处理
(function() {
  //-- Matching Begin.
  function Matching(element, params) {
    //缺省设置
    var settings = {
      canvasId: '',
      weightedId: '',
      append: true
    };

    var regex = new RegExp(/[\x20\u3000]/); //半角全角空格
    var lateWords = [];

    //---
    if ( params !== undefined ) {
      for ( var prop in params ) {
        if ( params.hasOwnProperty(prop) && settings.hasOwnProperty(prop)) {
          settings[prop] = params[prop];
        }
      }
    }

    console.log("canvasId " + settings.canvasId);
    var input = _(element.id);

    //输入一个字母
    input.oninput = function () {
      var value = input.value;
      if (regex.test(value.slice(-1))) { //输入空格
        lateWords = value.split(/[\u{20}\u{3000}]/u); //输入词组
        lateWords.sort();
        lateWords = _arrayUnique(lateWords); //删除重复词组
        console.log("lateWords: " + lateWords.length);
        TagCanvas.Reload(settings.canvasId);
        console.log("TagCanvas.Reload('" + settings.canvasId +"') OK");
      }
    };

    //输入回车换行
    input.onchange = function(event) {
      var inputed = input.value.split(/[\u{20}\u{3000}]/u); //输入完的词组
      inputed.sort();
      inputed = _arrayUnique(inputed); //删除重复词组
      if (!_arrayEqual(inputed, lateWords)) {
        lateWords = inputed;
        console.log("change: " + lateWords.length);
      }
    };


  };//-- Matching End.

  window.Matching = Matching;
}());

//定义jQuery插件
if ( typeof jQuery !== 'undefined' ) {
    ( function( $ ) {
        $.fn.matching = function( params ) {
            var args = arguments;
            return this.each( function() {
                if ( !$.data( this, 'plugin_Matching' ) ) {
                    $.data( this, 'plugin_Matching', new Matching(this, params ) );
                } else {
                    var plugin = $.data( this, 'plugin_Matching' );
                    if ( plugin[ params ] ) {
                        plugin[ params ].apply( this, Array.prototype.slice.call( args, 1 ) );
                    } else {
                        $.error( 'Method ' +  params + ' does not exist on jQuery.matching' );
                    }
                }
            } );
        };
    } ( jQuery ) );
}