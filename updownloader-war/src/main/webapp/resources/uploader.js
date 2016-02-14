;
(function ($, undefined) {
    'use strict';

    var UploadButton = function (rootElement, initParameters) {
        this.root = rootElement;
        this.tempData = null;

        console.log(this);

        this.refreshContentTable();
        this.uploadModule();
    };

    UploadButton.prototype.refreshContentTable = function () {
        var contenttable = $('.contenttable', this.root);
        var contenttable_tbody = contenttable.find('tbody');

        $.ajax({
            url: '/servlet/getcontent',
            data: this.tempData,
            type: 'get',
            processData: false,
            contentType: false,
            success: $.proxy(function (result, status, xhr) {
                contenttable_tbody.empty();


                $.each(result.folders, function (i, folder) {
                    var id = result.level + "_" + i;
                    contenttable_tbody.append("<tr data-id='" + id + "'><td colspan='3'><span class='glyphicon glyphicon-folder-close'></span>&nbsp;" + folder.name + "</td></tr>");

                    var tr = contenttable_tbody.find("tr[data-id=" + id + "]");
                    tr.on('click', $.proxy(function (e) {
                        console.log(e);
                        console.log(e.currentTarget);
                        console.log(folder);

                        $.each(folder.files, function (i, file) {
                            var str = "<tr><td colspan='3'>";
                            for (var idx = 0; idx < file.level; idx++) {
                                str += "&nbsp;&nbsp;"
                            }
                            str += "<span class='glyphicon glyphicon-file'></span>" + file.name + "</td></tr>";
                            tr.after(str);
                        });

                        tr.off('click');
                    }, this));

                });

                $.each(result.files, function (i, file) {
                    contenttable_tbody.append(
                        "<tr><td><a href='/servlet/downloader?file=" + file.name + "'><span class='glyphicon glyphicon-file'></span>&nbsp;" + file.name +
                        "</a></td><td>" + filesizeToString(file.size) +
                        "</td><td>" + file.mimetype + "</td></tr>");
                });
            }, this),
            error: function (xhr, status, error) {
                contenttable_tbody.empty();

                console.log(xhr);
                console.log(error);
                contenttable_tbody.append("<tr><td colspan='3' align='center' style='color:#A00'>Ошибка обработки запроса (" + xhr.statusText + " - " + xhr.status + ") !</td></tr>");
            }
        });
    }

    UploadButton.prototype.uploadModule = function () {
        var uploadBtn = $('.uploadbtn', this.root);
        var uploadModal = $('.uploadmodal', this.root);

        var uploadModalSubmit = $('.btn-upload', uploadModal);

        var uploadFormGroup = uploadModal.find('.newdocument-form-group');
        var uploadTable = uploadFormGroup.find('.uploadtable');
        var newDocumentDiv = uploadFormGroup.find('.uploaddnd');
        var newDocumentFileChooser = uploadFormGroup.find('.filechooser');
        var newDocumentProgress = uploadFormGroup.find('.ecmprogress');
        var newDocumentProgressLine = newDocumentProgress.find('.progress-bar');

        uploadBtn.on('click', function () {
            console.log('show');
            uploadModal.modal('show');
        });

        $('form', uploadModal).submit($.proxy(function (event) {
            event.preventDefault();

            uploadFormGroup.removeClass('has-error');
            var hasError = false;

            if (this.tempData == null) {
                uploadFormGroup.addClass('has-error');
                hasError = true;
            }

            if (!hasError) {
                uploadModalSubmit.attr('disabled', 'disabled');

                $.ajax({
                    url: '/servlet/uploader',
                    data: this.tempData,
                    type: 'post',
                    processData: false,
                    contentType: false,
                    xhr: function () {
                        var myXhr = $.ajaxSettings.xhr();

                        myXhr.upload.onprogress = function (evt) {
                            var prgrs = evt.loaded / evt.total * 100;

                            newDocumentProgressLine.css('width', Math.round(prgrs) + '%');
                        };
                        myXhr.upload.onloadstart = function () {
                            newDocumentProgress.show();
                        };
                        myXhr.upload.onloadend = function () {
                            newDocumentProgress.hide();
                            newDocumentProgressLine.css('width', '0%');
                        };

                        return myXhr;
                    },
                    success: $.proxy(function (result, status, xhr) {
                        console.log(result);

                        uploadModal.modal('hide');
                        uploadModalSubmit.removeAttr('disabled');

                        this.tempData = null;

                        this.refreshContentTable();
                    }, this),
                    error: function (xhr, status, error) {
                        alert("Ошибка обработки запроса!");
                        newDocumentButton.removeAttr('disabled');
                    }
                });
            }
        }, this));

        var newDocumentFileTableRefresh = $.proxy(function () {
            uploadTable.find('tbody').empty();

            var iter = this.tempData.values();

            var item;
            var length = 0;
            while (item = iter.next()) {
                if (item.done == true) break;
                console.log(item.value);

                var file = item.value;

                uploadTable.append('<tr><th scope="row">' + ++length + '</th><td>' + file.name + '</td><td>' + filesizeToString(file.size) + '</td><td>' + file.type + '</td><td>' +
                    '<button data-item="' + file.name + '" type="button" class="btn btn-default btn-delete-item"><span class="glyphicon glyphicon-remove"></span></button>' +
                    '</td></tr>');

                var deleteItem = uploadTable.find('.btn-delete-item');
                deleteItem.off('click');
                deleteItem.on('click', $.proxy(function (e) {
                    console.log(e);
                    var item = $(e.currentTarget).data("item");
                    console.log("file_" + item);

                    this.tempData.delete("file_" + item);

                    newDocumentFileTableRefresh();
                }, this));
            }
        }, this);

        uploadModal.on('hidden.bs.modal', $.proxy(function (e) {
            uploadFormGroup.removeClass('has-error');
            uploadTable.find('tbody').empty();
            this.tempData = null;
        }, this));

        newDocumentDiv.on('dragover', $.proxy(function (e) {
            e.stopPropagation();
            e.preventDefault();
            newDocumentDiv.addClass('uploaddnd-over');
        }, this));

        newDocumentDiv.on('drop', $.proxy(function (e) {
            e.preventDefault();
            var files = e.originalEvent.dataTransfer.files;

            this.tempData = addToData(this.tempData, files);
            newDocumentFileTableRefresh();

            newDocumentDiv.removeClass('uploaddnd-over');
        }, this));

        newDocumentDiv.on('dragleave', $.proxy(function (e) {
            e.stopPropagation();
            e.preventDefault();
            newDocumentDiv.removeClass('uploaddnd-over');
        }, this));

        newDocumentDiv.on('click', $.proxy(function (e) {
            e.stopPropagation();
            e.preventDefault();
            $('.filechooser', uploadModal).trigger('click');
        }, this));

        newDocumentFileChooser.on('change', $.proxy(function (e) {
            var files = e.target.files;

            this.tempData = addToData(this.tempData, files);
            newDocumentFileTableRefresh();
        }, this));
    }

    var sprintf = function (str) {
        var args = arguments,
            i = 1;

        str = str.replace(/%s/g, function () {
            var arg = args[i++];

            if (typeof arg === 'undefined') {
                return '';
            }

            return arg;
        });

        return str;
    };

    var addToData = function (formData, files) {
        if (formData == null) {
            formData = new FormData();
        }

        $.each(files, function (i, file) {
            var key = 'file_' + file.name;

            if (formData.has(key)) {
                formData.delete(key)
            }

            formData.append(key, file);
        });

        return formData;
    };

    var filesizeToString = function (size) {
        var result = "";

        var sizeKB = size / 1024;

        if (sizeKB > 1024) {
            var sizeMB = sizeKB / 1024;
            result = sizeMB.toFixed(2) + " MB";
        } else {
            result = sizeKB.toFixed(2) + " KB";
        }

        return result;
    };

    var pluginName = "UploadButton";
    $.fn[pluginName] = function (options) {
        var result = this;

        this.each(function () {
            var _this = $.data(this, pluginName);
            if (typeof options == 'undefined') {
                result = _this;
            } else {
                $.data(this, pluginName, new UploadButton(this, $.extend(true, {}, options)));
            }
        });

        return result;
    };
})
(jQuery);
