if(!$('#jqueryInputMaskJs').length) {
    var s = document.createElement("script");
    s.id = "jqueryInputMaskJs";
    s.type = "text/javascript";
    s.src = "../evs-mbarara/resources/js/jquery.inputmask.js";
    $("head").append(s);
}

if(!$('#inputMaskJs').length) {
    var s = document.createElement("script");
    s.id = "inputMaskJs";
    s.type = "text/javascript";
    s.src = "../evs-mbarara/resources/js/inputmask.js";
    $("head").append(s);
}

$scope.showAddInstanceButton = false;
$scope.showDeleteInstanceButton = false;
$scope.showLookupButton = true;
$scope.showImportButton = false;

$scope.showFieldSelect = true;

$scope.exportTaskId = null;
$scope.exportProgress = 0;
$scope.exportStatusTimer = null;

if ($scope.selectedEntity.name === "Participant") {
    $scope.showBackToEntityListButton = false;
    $.ajax({
      url: '../evs-mbarara/checkSubjectImportPermissions',
      success:  function() {
        $scope.showImportButton = true;
      },
      async: false
    });
} else {
    $scope.showViewTrashButton = false;
    $scope.backToEntityList = function() {
        $scope.dataRetrievalError = false;
        $scope.selectedEntity = undefined;
        window.location.replace('#/evsMbarara/reports');
    };
}

if ($scope.selectedEntity.name === "Participant") {
    $rootScope.selectedTab = "subjects";
} else {
    $rootScope.selectedTab = "reports";
}

var importCsvModal = '../evs-mbarara/resources/partials/modals/import-csv.html';
var exportModal = '../evs-mbarara/resources/partials/modals/export-entity.html';

$scope.customModals.push(importCsvModal);
$scope.customModals.push(exportModal);

$scope.importEntityInstances = function() {
    $('#importSubjectModal').modal('show');
};

$scope.importSubject = function () {
    blockUI();

    $('#importSubjectForm').ajaxSubmit({
        success: function () {
            $("#instancesTable").trigger('reloadGrid');
            $('#importSubjectForm').resetForm();
            $('#importSubjectModal').modal('hide');
            unblockUI();
        },
        error: function (response) {
            handleResponse('mds.error', 'mds.error.importCsv', response);
        }
    });
};

$scope.closeImportSubjectModal = function () {
    $('#importSubjectForm').resetForm();
    $('#importSubjectModal').modal('hide');
};

$scope.closeExportEvsMbararaInstanceModal = function () {
    $scope.cancelExport();

    $('#exportEvsMbararaInstanceForm').resetForm();
    $('#exportEvsMbararaInstanceModal').modal('hide');
};

$scope.exportEntityInstances = function () {
    $scope.checkboxModel.exportWithLookup = true;
    $('#exportEvsMbararaInstanceModal').modal('show');
};

$scope.saveFile = function (data, filename, type) {
    var file = new Blob([data], {type: type});

    if (window.navigator.msSaveOrOpenBlob) // IE10+
        window.navigator.msSaveOrOpenBlob(file, filename);
    else { // Others
        var a = document.createElement("a"),
          url = URL.createObjectURL(file);
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        setTimeout(function() {
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        }, 0);
    }
};

$scope.finishExport = function() {
    $scope.exportProgress = 0;
    $scope.exportTaskId = null;

    if ($scope.exportStatusTimer) {
        clearInterval($scope.exportStatusTimer);
        $scope.exportStatusTimer = null;
    }
};

$scope.cancelExport = function() {
    if ($scope.exportTaskId) {
        $http.get("../evs-mbarara/export/" + $scope.exportTaskId + "/cancel");
        $scope.finishExport();
    }
};

$scope.checkExportStatus = function() {
    $http.get("../evs-mbarara/export/" + $scope.exportTaskId + "/status")
      .success(function (data) {
          $scope.exportProgress = Math.floor(data.progress * 100);

          if (data.status === 'FAILED' || data.status === 'CANCELED') {
              $scope.finishExport();
              motechAlert('mds.error', 'mds.error.exportData');
          } else if (data.status === 'FINISHED') {
              $http.get("../evs-mbarara/export/" + $scope.exportTaskId + "/results", { responseType: 'blob' })
                .success(function (data, status, headers) {
                    $('#exportEvsMbararaInstanceForm').resetForm();
                    $('#exportEvsMbararaInstanceModal').modal('hide');

                    var fileType = headers('Content-Type');
                    var fileName = 'instance.' + $scope.exportFormat;

                    var contentDisposition = headers('Content-Disposition');
                    var filenameRegex = /filename[^;=\n]*=([\w.]*)/;
                    var matches = filenameRegex.exec(contentDisposition);

                    if (matches != null && matches[1]) {
                        fileName = matches[1];
                    }

                    $scope.saveFile(data, fileName, fileType);
                })
                .error(function (response) {
                    handleResponse('mds.error', 'mds.error.exportData', response);
                });

              $scope.finishExport();
          }
      })
      .error(function (response) {
          $scope.finishExport();
          handleResponse('mds.error', 'mds.error.exportData', response);
      });
};

$scope.exportInstance = function() {
    var selectedFieldsName = [], url, sortColumn, sortDirection;

    url = "../evs-mbarara/entities/" + $scope.selectedEntity.id + "/exportInstances";
    url = url + "?outputFormat=" + $scope.exportFormat;
    url = url + "&exportRecords=" + $scope.actualExportRecords;

    if ($scope.actualExportColumns === 'selected') {
        angular.forEach($scope.selectedFields, function(selectedField) {
            selectedFieldsName.push(selectedField.basic.displayName);
        });

        url = url + "&selectedFields=" + selectedFieldsName;
    }

    if ($scope.checkboxModel.exportWithOrder === true) {
        sortColumn = $('#instancesTable').getGridParam('sortname');
        sortDirection = $('#instancesTable').getGridParam('sortorder');

        url = url + "&sortColumn=" + sortColumn;
        url = url + "&sortDirection=" + sortDirection;
    }

    if ($scope.checkboxModel.exportWithLookup === true) {
        url = url + "&lookup=" + (($scope.selectedLookup) ? $scope.selectedLookup.lookupName : "");
        url = url + "&fields=" + JSON.stringify($scope.lookupBy);
    }

    $scope.exportProgress = 0;

    $http.get(url)
      .success(function (data) {
          $scope.exportTaskId = data;

          setTimeout(function(){$scope.checkExportStatus()}, 1500);
          $scope.exportStatusTimer = setInterval(function(){$scope.checkExportStatus()}, 5000);
      })
      .error(function (response) {
          $scope.finishExport();
          handleResponse('mds.error', 'mds.error.exportData', response);
      });
};

$scope.editInstance = function(id, module, entityName) {
    blockUI();
    $scope.setHiddenFilters();

    $scope.instanceEditMode = true;

    $scope.setModuleEntity(module, entityName);
    $scope.loadedFields = Instances.selectInstance({
        id: $scope.selectedEntity.id,
        param: id
        },
        function (data) {
            $scope.selectedInstance = id;
            $scope.currentRecord = data;
            $scope.fields = data.fields;

            unblockUI();
        }, angularHandler('mds.error', 'mds.error.cannotUpdateInstance'));
};

$scope.saveCurrentRecord = function() {
    $scope.currentRecord.$save(function() {
        $scope.unselectInstance();
        unblockUI();
    }, angularHandler('mds.error', 'mds.error.cannotAddInstance'));
};

$scope.addEntityInstanceDefault = function () {
    blockUI();

    var entityObject = {};

    var values = $scope.currentRecord.fields;
    angular.forEach (values, function(value, key) {
        value.value = value.value === 'null' ? null : value.value;

        if (!$scope.isAutoGenerated(value)) {
            entityObject[value.name] = value.value;
        }
    });

    if ($scope.selectedEntity.name === "Participant" && $scope.selectedInstance !== undefined) {
        $http.post('../evs-mbarara/subjectDataChanged', entityObject)
          .success(function(response) {
              $scope.saveCurrentRecord();
          })
          .error(function(response) {
              motechAlert("evsMbarara.updateSubject.errorMsg", "evsMbarara.updateSubject.errorTitle", response);
              unblockUI();
          });
    } else {
        $scope.saveCurrentRecord();
    }
};

$scope.addEntityInstance = function () {
    if ($scope.selectedEntity.name === "Participant") {
        var input = $("#phoneNumberForm");
        var fieldValue = input.val();
        if (fieldValue !== null && fieldValue !== undefined && fieldValue !== '') {
            input.val(fieldValue.replace(/ /g, ''));
            input.trigger('input');
        }

        $scope.addEntityInstanceDefault();
    } else {
        $scope.addEntityInstanceDefault();
    }
};

$scope.showLookupDialog = function() {
    $("#lookup-dialog")
    .css({'top': ($("#lookupDialogButton").offset().top - $("#main-content").offset().top) - 40,
    'left': ($("#lookupDialogButton").offset().left - $("#main-content").offset().left) - 15})
    .toggle();
    $("div.arrow").css({'left': 50});
};

var isPhoneNumberForm = false;

$scope.loadEditValueFormDefault = $scope.loadEditValueForm;

$scope.loadEditValueForm = function (field) {
    if (field.name === 'phoneNumber') {
        isPhoneNumberForm = true;
        return '../evs-mbarara/resources/partials/widgets/field-phone-number.html';
    } else if (field.name === 'visits') {
        return '../evs-mbarara/resources/partials/widgets/field-visits.html';
    } else if (field.name === 'enrollment') {
        return '../evs-mbarara/resources/partials/widgets/field-enrollment.html';
    }

    if (isPhoneNumberForm) {
        $("#phoneNumberForm").inputmask({ mask: "999 999 999[ 999]", greedy: false, autoUnmask: true });
        isPhoneNumberForm = false;
    }

    return $scope.loadEditValueFormDefault(field);
};

$scope.msg = function () {
    if (arguments !== undefined && arguments !== null && arguments.length === 1) {
        if (arguments[0] === 'mds.btn.lookup') {
            arguments[0] = 'evsMbarara.btn.lookup';
        } else if (arguments[0] === 'mds.form.label.lookup') {
            arguments[0] = 'evsMbarara.form.label.lookup';
        }
    }
    return $scope.$parent.msg.apply(null, arguments);
};

$scope.retrieveAndSetEntityData = function(entityUrl, callback) {
    $scope.lookupBy = {};
    $scope.selectedLookup = undefined;
    $scope.lookupFields = [];
    $scope.allEntityFields = [];

    blockUI();

    $http.get(entityUrl).success(function (data) {
        $scope.selectedEntity = data;

        $scope.setModuleEntity($scope.selectedEntity.module, $scope.selectedEntity.name);

        $http.get('../mds/entities/'+$scope.selectedEntity.id+'/entityFields').success(function (data) {
            $scope.allEntityFields = data;
            $scope.setAvailableFieldsForDisplay();

            if ($routeParams.entityId === undefined) {
                var hash = window.location.hash.substring(2, window.location.hash.length) + "/" + $scope.selectedEntity.id;
                $location.path(hash);
                $location.replace();
                window.history.pushState(null, "", $location.absUrl());
            }

            Entities.getAdvancedCommited({id: $scope.selectedEntity.id}, function(data) {
                $scope.entityAdvanced = data;
                $rootScope.filters = [];
                $scope.setVisibleIfExistFilters();

                if ($scope.selectedEntity.name === "Participant") {
                    $http.get("../evs-mbarara/getLookupsForSubjects")
                        .success(function(data) {
                            $scope.entityAdvanced.indexes = data;
                        });
                } else {
                    $(".clearfix").children(".btn-primary").html("<i class='fa fa-lg fa-level-up'></i>&nbsp;" + $scope.msg('evsMbarara.reports.btn.backToList'));
                }

                var filterableFields = $scope.entityAdvanced.browsing.filterableFields,
                    i, field, types;
                for (i = 0; i < $scope.allEntityFields.length; i += 1) {
                    field = $scope.allEntityFields[i];

                    if ($.inArray(field.id, filterableFields) >= 0) {
                        types = $scope.filtersForField(field);

                        $rootScope.filters.push({
                            displayName: field.basic.displayName,
                            type: field.type.typeClass,
                            field: field.basic.name,
                            types: types
                        });
                    }
                }
                $scope.selectedFields = [];
                for (i = 0; i < $scope.allEntityFields.length; i += 1) {
                    field = $scope.allEntityFields[i];
                    if ($.inArray(field.basic.name, $scope.entityAdvanced.userPreferences.visibleFields) !== -1) {
                        $scope.selectedFields.push(field);
                    }
                }
                $scope.updateInstanceGridFields();

                if (callback) {
                    callback();
                }

                unblockUI();
            });
        });
        unblockUI();
    });
};
