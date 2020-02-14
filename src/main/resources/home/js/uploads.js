"use strict";
// Upload File Reader : https://www.w3.org/TR/file-upload/#dfn-filereader
// Bootstrap Modal : https://getbootstrap.com/docs/4.0/components/modal/
//                   https://www.w3schools.com/bootstrap/bootstrap_modal.asp
// See https://fontawesome.com/icons?d=gallery
// See https://webllica.com/search-font-awesome-icon-by-japanese/
// See https://www.html5rocks.com/zh/tutorials/file/dndfiles/
// Inner functions
function _(el) {return document.getElementById(el);} // Like jQuery $('#id')
function _c(c) {return document.querySelector(c);}
function _toConsumableArray(arr) { return _arrayWithoutHoles(arr) || _iterableToArray(arr) || _nonIterableSpread(); }
function _nonIterableSpread() { throw new TypeError("Invalid attempt to spread non-iterable instance"); }
function _iterableToArray(iter) { if (Symbol.iterator in Object(iter) || Object.prototype.toString.call(iter) === "[object Arguments]") return Array.from(iter); }
function _arrayWithoutHoles(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = new Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } }
function _formatSizeUnits(bytes){
  if      (bytes >= 1073741824) { bytes = (bytes / 1073741824).toFixed(2) + " GB"; }
  else if (bytes >= 1048576)    { bytes = (bytes / 1048576).toFixed(2) + " MB"; }
  else if (bytes >= 1024)       { bytes = (bytes / 1024).toFixed(2) + " KB"; }
  else if (bytes > 1)           { bytes = bytes + " bytes"; }
  else if (bytes == 1)          { bytes = bytes + " byte"; }
  else                          { bytes = "0 bytes"; }
  return bytes;
}
function _getExtension(filename) {
  var parts = filename.split('.');
  return parts[parts.length - 1];
}

var uploadOptions = {
  uploadUrl: '/upload',  // Server upload URI
  maxFileCount: 2,
  maxFileSize: 1024, // max file size in KBs
  allowedFileExtensions: ["pdf"],
  token: '',
  success: function(){},
  error: function(){},
  complete: function(){}
}

var Outcome = {
  error: '',
  errorkeys: [],
  preview: [],
  config: {},
  append: 'false'
}

function initUploadOptions(options) {
  console.log("initUploadOptions...");
  uploadOptions = options;
}

function getCookies(){
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

function getCookie(key){
  return getCookies()[key];
}

function getUploadToken(){
  //TODO
  // Set token cookie
  console.log("set token cookie...");
  return "12345";
}

window.onload = function () {
  var tableWrapper = _c('.table-wrapper');
  var reserved = _c('.upload-table');
  var completed = _c('.completed-table');
  var btnClearAll = _('clear-all');
  var btnUploadFiles = _('upload-files');
  var uploadArea = _c('.upload-area'); //FILE API - HTML5 new API
  var previewModal = _('ModalCenter');
  var allowedFileExtensions = uploadOptions.allowedFileExtensions;
  //File, FileList, FileReader
  var previewFileIconSettings = {  // configure your icon file extensions
    'doc': 'fas fa-file-word fa-2x text-primary',
    'xls': 'fas fa-file-excel fa-2x text-success',
    'ppt': 'fas fa-file-powerpoint fa-2x text-danger',
    'pdf': 'fas fa-file-pdf fa-2x text-danger',
    'zip': 'fas fa-file-archive fa-2x text-muted',
    'htm': 'fas fa-file-code fa-2x text-info',
    'txt': 'fas fa-file-alt fa-2x text-info',
    'mov': 'fas fa-file-video fa-2x text-warning',
    'mp3': 'fas fa-file-audio fa-2x text-warning',
    // note for these file types below no extension determination logic
    // has been configured (the keys itself will be used as extensions)
    'jpg': 'fas fa-file-image fa-2x text-danger',
    'gif': 'fas fa-file-image fa-2x text-muted',
    'png': 'fas fa-file-image fa-2x text-primary'
  };

  var previewFileExtSettings = { // configure the logic for determining icon file extensions
      'doc': function(ext) {return ext.match(/(doc|docx)$/i);},
      'xls': function(ext) {return ext.match(/(xls|xlsx)$/i);},
      'ppt': function(ext) {return ext.match(/(ppt|pptx)$/i);},
      'zip': function(ext) {return ext.match(/(zip|rar|tar|gzip|gz|7z)$/i);},
      'htm': function(ext) {return ext.match(/(htm|html)$/i);},
      'txt': function(ext) {return ext.match(/(txt|ini|csv|java|php|js|css)$/i);},
      'mov': function(ext) {return ext.match(/(avi|mpg|mkv|mov|mp4|3gp|webm|wmv)$/i);},
      'mp3': function(ext) {return ext.match(/(mp3|wav)$/i);}
  };
  var uploadedFileCount = 0;
  var uploadedFileSize  = 0;
  var reservedFiles = [];
  var completedFileCount = 0;
  var completedFiles = [];

  var btnChartFiles = _('chart-files');
  //Disable chart button
  btnChartFiles.disabled = true;
  // Click Chart Files Button
  btnChartFiles.onclick = function () {
    // Edit show resume score link
    var svg = "/v1/chart/CwoqFQArtoRdNXYJMdpzv3lCepiTDrAt8TFYI4/CwoqKr8hmIy2QxOHU8euCBU0M7jA77JkTBchMW/CxjsGYYcPZfHl2vJpaMFlxYiNH3WDzpx8eVr7t/CwotWpW7cKAX1s2Lj6ZbPyOqzcoEghF1qv1WkP";
    var img = '<object data="' + svg + '" type="image/svg+xml"></object>';
    //var img = '<img src="' + svg + '"></img>';
    $('#chart-body').html(img);
    $('#chart-modal').modal();
  };

  _c('.upload').onclick = function () {
    this.nextElementSibling.click();
  };

  // Click Add More Files
  _('more-files').onclick = function () {
    _('fileUpload').click();
  };

  // Click Clear All
  btnClearAll.onclick = function () {
    tableWrapper.classList.remove("show");
    setTimeout(function () {
      reserved.lastElementChild.innerHTML = "";
      completed.lastElementChild.innerHTML = "";
      uploadedFileCount = 0;
      uploadedFileSize  = 0;
      completedFileCount = 0;
      reservedFiles = [];
      completedFiles = [];
    }, 1000);
  };

  // Click Upload Files
  btnUploadFiles.onclick = function () {
    console.log("Click Upload Files... " + reservedFiles.length);
    for (var i=0; i<reservedFiles.length; i++) {
      console.log("Uploading " + reservedFiles[i].name);
      _uploadFile(reservedFiles[i]);
    }

    //Disable upload button
    btnUploadFiles.disabled = true;
    btnUploadFiles.classList.remove("btn-outline-success");
    btnUploadFiles.classList.add("btn-outline-dark");

    // Clear upload table
    var emptyTbody = document.createElement('tbody');
    reserved.replaceChild(emptyTbody, reserved.tBodies[0]);
    uploadedFileCount = 0;
    uploadedFileSize  = 0;
    reservedFiles = [];
  };

  // Drug & Drop files
  uploadArea.ondragover = function (e) {
    e.preventDefault();
    this.classList.add("dragover");
  };

  uploadArea.ondragleave = function (e) {
    this.classList.remove("dragover");
  };

  uploadArea.ondrop = function (e) {
    e.preventDefault();
    FilesUploader(_toConsumableArray(e.dataTransfer.files));
    tableWrapper.classList.add("show");
    uploadArea.classList.remove("dragover");
  }; //listen to the change event of file

  _('fileUpload').onchange = function (e) {
    FilesUploader(_toConsumableArray(e.target.files));
    tableWrapper.classList.add("show");
  };

  function _getPreviewIcon(file) {
    var ext = _getExtension(file.name);
    var out = previewFileIconSettings[ext] || previewFileIconSettings[ext.toLowerCase()] || null;
    if (out != null)
      return out;
    $.each(previewFileExtSettings, function (key, func) {
      if (previewFileIconSettings[key] && func(ext)) {
        out = previewFileIconSettings[key];
        return;
      }
    });
    return out;
  }

  //Check file type
  function _fileTypeValidation(file) {
    var ext = _getExtension(file.name);
    var validation = (allowedFileExtensions.includes(ext));
    if (!validation) {
      $.each(previewFileExtSettings, function (key, func) {
          if (previewFileIconSettings[key] && func(ext)) {
            validation = true;
            return;
          }
      });
    }
    return validation;
  }

  //Check file size and count
  function _fileLimitation(file) {
    if ((uploadedFileCount + 1) > uploadOptions.maxFileCount) {
      return true;
    }
    if ((uploadedFileSize + file.size) > uploadOptions.maxFileSize*1024) {
      return true;
    }
    return false;
  }

  function _uploadFile(file) {
      var formData = new FormData();
      formData.append("file", file);
      formData.append("fileId", file.size + "_" + file.name);
      $.ajax({
        url: uploadOptions.uploadUrl,
        type: 'POST',
        data: formData,
        cache: false,
        dataType: 'json',
        timespan: 1000,
        processData: false, // Don't process the files - I actually got this and the next from an SO post but I don't remember where
        contentType: false, // Set content type to false as jQuery will tell the server its a query string request
        success: function (response) {
          console.log("success: " + JSON.stringify(response));
          _completeFile(file, response);
        },
        error: function () {
          _completeFile(file, {
            error: "Internal Server error",
            append: 'false'
          });
        },
        complete: function () {
        }
      });
  }

  //等待上传的文件列表
  function FilesUploader(files){
    files.forEach(function (file) {
      //Check file type, size and count
      if (_fileTypeValidation(file)) {
        var reader = new FileReader();
        var limited = false;
        reader.onloadend = function (event) {
          limited = _fileLimitation(file);
          if (limited) // Count, Size over
            return;

          uploadedFileCount++;
          uploadedFileSize += file.size;
          //Enable upload button
          if (btnUploadFiles.disabled) {
            btnUploadFiles.disabled = false;
            btnUploadFiles.classList.remove("btn-outline-dark");
            btnUploadFiles.classList.add("btn-outline-success");
          }

          var tr = document.createElement('tr');
          var tdNo = document.createElement('td');
          tdNo.innerText = uploadedFileCount;
          var tdImage = document.createElement('td');
          var image = document.createElement('i');
          image.className = _getPreviewIcon(file);
          tdImage.appendChild(image);

          var tdName = document.createElement('td');
          tdName.innerText = file.name + " (" + _formatSizeUnits(file.size) + ")";
          tdName.classList.add("upload-file");

          var tdDelete = document.createElement('td');
          var icon = document.createElement('i');
          icon.className = "fas fa-trash-alt";
          tdDelete.appendChild(icon);

          // Trash a file
          icon.onclick = function () {
            //recursively go to all nextElementSiblings and update No.
            var currentTr = tr;

            var index = uploadedFileCount - 1;
            if (currentTr.nextElementSibling) {
              index = parseInt(currentTr.nextElementSibling.firstElementChild.innerText, 10);
              index = index - 2;
            }
            var array = reservedFiles;
            //Remove file size
            var size = array[index].size;
            array.splice(index, 1);
            reservedFiles = array;

            while (currentTr.nextElementSibling) {
              currentTr.nextElementSibling.firstElementChild.innerText -= 1;
              currentTr = currentTr.nextElementSibling;
            }
            tr.remove();
            //Count down
            if (uploadedFileSize > 0)
              uploadedFileSize -= size;
            if (uploadedFileCount > 0)
              uploadedFileCount--;

            if (0 == uploadedFileCount && 0 == completedFileCount) {
              btnClearAll.onclick();
            }
          };

          tr.appendChild(tdNo);
          tr.appendChild(tdImage);
          tr.appendChild(tdName);
          tr.appendChild(tdDelete);
          reserved.lastElementChild.appendChild(tr);
          reservedFiles.push(file);
        };

        if (!limited) {
          reader.readAsDataURL(file); // reader.onprogress = function(ev){}
        }
      }
    });
  }

  //处理上传完的文件 : 在上传完的文件列表里显示文件信息和处理结果的连接
  function _completeFile(file, outcome){
    var append = outcome.append == 'true';
      completedFileCount++;
      var tr = document.createElement('tr');
      var tdStatus = document.createElement('td');
      var status = document.createElement('i');
      if (append) {
        status.className = "fas fa-check-circle fa-lg text-success";
        //status.className = "fas fa-check-circle fa-lg color-" + completedFileCount;
      } else {
        status.className = "fas fa-times-circle fa-lg text-danger";
      }
      tdStatus.appendChild(status);

      var tdImage = document.createElement('td');
      var image = document.createElement('i');
      image.className = _getPreviewIcon(file);
      tdImage.appendChild(image);

      var tdName = document.createElement('td');

      tdName.innerText = file.name + " (" + _formatSizeUnits(file.size) + ")";
      tdName.classList.add("upload-file");

      var tdLink = document.createElement('td');
      var btnLink = document.createElement('button');
      btnLink.type = "button";
      btnLink.className = "btn";
      if (append) {
        // Create Button trigger modal
        // <button type="button" class="btn" data-toggle="modal" data-target="#ModalCenter"><i class="fas fa-search text-primary"></i></button>
        btnLink.setAttribute('data-toggle','modal');
        btnLink.setAttribute('data-target','#preview-modal');
        var icon = document.createElement('i');
        icon.className = "fas fa-search text-primary";
        btnLink.appendChild(icon);
        tdLink.appendChild(btnLink);

        // Click a link to search resume file assets
        icon.onclick = function () {
          // Edit show resume score link
          var ref = '<a href="' + outcome.preview[0] + '"><i class="fas fa-file-contract text-success"></i> Score</a>';
          //$('#ModalCenter').on('shown.bs.modal', function () {$('#button').trigger('focus')})
          $('#preview-body').html(ref);
          $('#preview-modal').modal();
        };
      } else {
        var icon = document.createElement('i');
        icon.className = "fas fa-unlink text-danger";
        btnLink.appendChild(icon);
        tdLink.appendChild(btnLink);
      }

      tr.appendChild(tdStatus);
      tr.appendChild(tdImage);
      tr.appendChild(tdName);
      tr.appendChild(tdLink);
      //Set background-color mark chart line color
      tr.classList.add("bg-" + completedFileCount);
      completed.lastElementChild.appendChild(tr);
      completedFiles.push(file);

      //Enable chart button
      console.log("Enable chart button " + completedFileCount + "  " + btnChartFiles.disabled);
      if (btnChartFiles.disabled && completedFileCount > 0) {
        btnChartFiles.disabled = false;
        btnChartFiles.classList.remove("btn-outline-dark");
        btnChartFiles.classList.add("btn-outline-primary");
      }
  }

};