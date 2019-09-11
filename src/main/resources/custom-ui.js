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

$scope.showBackToEntityListButton = false;
$scope.showAddInstanceButton = false;
$scope.showDeleteInstanceButton = false;
$scope.showLookupButton = true;

if ($scope.selectedEntity.name === "Participant") {
    $rootScope.selectedTab = "subjects";
} else {
    $rootScope.selectedTab = "visitLimitation";
    $scope.showFieldsButton = false;
    $scope.showImportButton = false;
    $scope.showExportButton = false;
    $scope.showViewTrashButton = false;
    $scope.showFiltersButton = false;
}

var importCsvModal = '../evs-mbarara/resources/partials/modals/import-csv.html';
var editSubjectModal = '../evs-mbarara/resources/partials/modals/edit-subject.html';

$scope.customModals.push(importCsvModal);
$scope.customModals.push(editSubjectModal);

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
    $('#exportEvsMbararaInstanceModal').resetForm();
    $('#exportEvsMbararaInstanceModal').modal('hide');
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

    $http.get(url)
        .success(function () {
            $('#exportInstanceForm').resetForm();
            $('#exportInstanceModal').modal('hide');
            window.location.replace(url);
        })
        .error(function (response) {
            handleResponse('mds.error', 'mds.error.exportData', response);
        });
};

$scope.showAdvanced = false;
$scope.advancedButtonIndex = 6;

$scope.showOrHideAdvanced = function() {
    var i;
    $scope.showAdvanced = !$scope.showAdvanced;

    for (i = $scope.advancedButtonIndex + 1; i < $scope.fields.length; i += 1) {
        $scope.fields[i].nonDisplayable = !$scope.showAdvanced;
    }
};

$scope.getAdvancedButtonLabel = function() {
    if ($scope.showAdvanced) {
        return $scope.msg('evsMbarara.hideAdvanced');
    } else {
        return $scope.msg('evsMbarara.showAdvanced');
    }
};

$scope.editInstance = function(id, module, entityName) {
    blockUI();
    $scope.setHiddenFilters();

    if (entityName === "Clinic") {
        $scope.instanceEditMode = false;
    } else {
        $scope.instanceEditMode = true;
    }

    $scope.setModuleEntity(module, entityName);
    $scope.loadedFields = Instances.selectInstance({
        id: $scope.selectedEntity.id,
        param: id
        },
        function (data) {
            $scope.selectedInstance = id;
            $scope.currentRecord = data;
            $scope.fields = data.fields;

            if (entityName === "Clinic") {
                $http.get('../evs-mbarara/evs-mbarara-config')
                .success(function(response) {
                    var i, j, fieldsMap = {},
                        clinicMainFields = response.clinicMainFields,
                        clinicExtendedFields = response.clinicExtendedFields,
                        showAdvancedButton = {
                            'name': 'showAdvanced',
                            'displayName': '',
                            'tooltip': '',
                            'value': '',
                            'type': {
                                'displayName': 'mds.field.string'
                            },
                            'required': false,
                            'nonEditable': false,
                            'nonDisplayable': false
                        };

                    $scope.showAdvanced = false;

                    for (i = 0; i < $scope.fields.length; i += 1) {
                        if ($scope.fields[i].name === "location" || $scope.fields[i].name === "siteId") {
                            $scope.fields[i].nonEditable = true;
                        }
                        fieldsMap[$scope.fields[i].displayName] = $scope.fields[i];
                    }

                    $scope.fields = [];

                    for (i = 0; i < clinicMainFields.length; i += 1) {
                        $scope.fields[i] = fieldsMap[clinicMainFields[i]];
                    }

                    $scope.advancedButtonIndex = i;
                    $scope.fields[i] = showAdvancedButton;

                    for (j = 0; j < clinicExtendedFields.length; j += 1) {
                        i += 1;
                        $scope.fields[i] = fieldsMap[clinicExtendedFields[j]];
                        $scope.fields[i].nonDisplayable = true;
                    }
                });
            } else if (entityName === "Participant") {
                var i;
                for (i = 0; i < $scope.fields.length; i += 1) {
                    if ($scope.fields[i].name === "changed") {
                        $scope.fields[i].nonDisplayable = true;
                    }
                }
            }

            unblockUI();
        }, angularHandler('mds.error', 'mds.error.cannotUpdateInstance'));
};

$scope.addEntityInstanceDefault = function () {
    blockUI();

    var values = $scope.currentRecord.fields;
    angular.forEach (values, function(value, key) {
        value.value = value.value === 'null' ? null : value.value;

        if (value.name === "changed") {
            value.value = true;
        }
    });

    $scope.currentRecord.$save(function() {
        $scope.unselectInstance();
        unblockUI();
    }, angularHandler('mds.error', 'mds.error.cannotAddInstance'));
};

$scope.addEntityInstance = function () {
    if ($scope.selectedEntity.name === "Participant") {
        var input = $("#phoneNumberForm");
        var fieldValue = input.val();
        if (fieldValue !== null && fieldValue !== undefined && fieldValue !== '') {
            input.val(fieldValue.replace(/ /g, ''));
            input.trigger('input');
        }

        $http.get('../evs-mbarara/evs-mbarara-config')
            .success(function(response){
                if(response.showWarnings) {
                    $('#editSubjectModal').modal('show');
                } else {
                    $scope.addEntityInstanceDefault();
                }
            })
            .error(function(response) {
                $('#editSubjectModal').modal('show');
            });
    } else {
        $scope.fields.splice($scope.advancedButtonIndex, 1);
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
    if (field.name === 'showAdvanced') {
        return '../evs-mbarara/resources/partials/widgets/field-show-advanced.html';
    } else if (field.name === 'phoneNumber') {
        isPhoneNumberForm = true;
        return '../evs-mbarara/resources/partials/widgets/field-phone-number.html';
    } else if (field.name === 'visits') {
        return '../evs-mbarara/resources/partials/widgets/field-visits.html';
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
