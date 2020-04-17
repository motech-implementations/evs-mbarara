(function() {
    'use strict';

    var controllers = angular.module('evsMbarara.controllers', []);

    controllers.controller('EvsMbararaUnscheduledVisitCtrl', function ($scope, $timeout, $http, $filter, AllParticipants) {

        $scope.getLookups("../evs-mbarara/unscheduledVisits/getLookupsForUnscheduled");

        $scope.participants = AllParticipants.query();

        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[0];

        $scope.newForm = function(type) {
            $scope.form = {};
            $scope.form.type = type;
            $scope.form.dto = {};
        };

        $scope.addUnscheduled = function() {
            $scope.newForm("add");
            $('#unscheduledVisitModal').modal('show');
            $scope.reloadSelects();
        };

        $scope.saveUnscheduledVisit = function(ignoreLimitation) {
            function sendRequest() {
                $http.post('../evs-mbarara/unscheduledVisits/new/' + ignoreLimitation, $scope.form.dto)
                    .success(function(data){
                        if (data && (typeof(data) === 'string')) {
                            jConfirm($scope.msg('evsMbarara.uncheduledVisit.confirmMsg', data), $scope.msg('evsMbarara.uncheduledVisit.confirmTitle'),
                                function (response) {
                                    if (response) {
                                        $scope.saveUnscheduledVisit(true);
                                    }
                                });
                        } else {
                            $("#unscheduledVisit").trigger('reloadGrid');
                            $scope.form.updated = data;
                            $scope.form.dto = undefined;
                        }
                    })
                    .error(function(response) {
                        motechAlert('evsMbarara.uncheduledVisit.scheduleError', 'evsMbarara.error', response);
                    });
            }

            if (ignoreLimitation) {
                sendRequest();
            } else {
                var confirmMsg;
                if ($scope.form.type === "add") {
                    confirmMsg = "evsMbarara.uncheduledVisit.confirm.shouldAddVisit";
                } else if ($scope.form.type === "edit") {
                    confirmMsg = "evsMbarara.uncheduledVisit.confirm.shouldUpdateVisit";
                }

                motechConfirm(confirmMsg, "evsMbarara.confirm",
                    function(confirmed) {
                        if (confirmed) {
                            sendRequest();
                        }
                })
            }
        };

        $scope.formIsFilled = function() {
            return $scope.form
                && $scope.form.dto
                && $scope.form.dto.participantId
                && $scope.form.dto.date;
        };

        $scope.reloadSelects = function() {
            $timeout(function() {
                $('#participantSelect').trigger('change');
            });
        };

        $scope.setPrintData = function(document, rowData) {
            $('#versionDate', document).html($filter('date')(new Date(), $scope.cardDateTimeFormat));
            $('#subjectId', document).html(rowData.participantId);
            $('#date', document).html($filter('date')($scope.parseDate(rowData.date), $scope.cardDateFormat));
        };

        $scope.printFrom = function(source) {

            if (source === "updated") {
                rowData = $scope.form.updated;
            } else {
                var rowData = jQuery("#unscheduledVisit").jqGrid ('getRowData', source);
            }

            var winPrint = window.open("../evs-mbarara/resources/partials/card/unscheduledVisitCard.html");
             if ((!(window.ActiveXObject) && "ActiveXObject" in window) || (navigator.userAgent.indexOf("MSIE") > -1)) {
             	// iexplorer
                 var windowOnload = winPrint.onload || function() {
                    setTimeout(function(){
                        $scope.setPrintData(winPrint.document, rowData);
                        winPrint.focus();
                        winPrint.print();
                    }, 500);
                 };

                 winPrint.onload = new function() { windowOnload(); } ;
             } else {

                winPrint.onload = function() {
                    $scope.setPrintData(winPrint.document, rowData);
                    winPrint.focus();
                    winPrint.print();
                }
             }
        };

        $scope.exportInstance = function() {
                    var sortColumn, sortDirection, url = "../evs-mbarara/exportInstances/unscheduledVisits";
                    url = url + "?outputFormat=" + $scope.exportFormat;
                    url = url + "&exportRecords=" + $scope.actualExportRecords;

                    if ($scope.checkboxModel.exportWithFilter === true) {
                        url = url + "&dateFilter=" + $scope.selectedFilter.dateFilter;

                        if ($scope.selectedFilter.startDate) {
                            url = url + "&startDate=" + $scope.selectedFilter.startDate;
                        }

                        if ($scope.selectedFilter.endDate) {
                            url = url + "&endDate=" + $scope.selectedFilter.endDate;
                        }
                    }

                    if ($scope.checkboxModel.exportWithOrder === true) {
                        sortColumn = $('#unscheduledVisit').getGridParam('sortname');
                        sortDirection = $('#unscheduledVisit').getGridParam('sortorder');

                        url = url + "&sortColumn=" + sortColumn;
                        url = url + "&sortDirection=" + sortDirection;
                    }

                    $scope.exportInstanceWithUrl(url);
                };
    });

    controllers.controller('EvsMbararaBaseCtrl', function ($scope, $timeout, $http, MDSUtils) {

        $scope.filters = [{
            name: $scope.msg('evsMbarara.today'),
            dateFilter: "TODAY"
        },{
            name: $scope.msg('evsMbarara.tomorrow'),
            dateFilter: "TOMORROW"
        },{
            name: $scope.msg('evsMbarara.twoDaysAfter'),
            dateFilter: "TWO_DAYS_AFTER"
        },{
            name: $scope.msg('evsMbarara.nextThreeDays'),
            dateFilter: "NEXT_THREE_DAYS"
        },{
            name: $scope.msg('evsMbarara.thisWeek'),
            dateFilter: "THIS_WEEK"
        },{
            name: $scope.msg('evsMbarara.dateRange'),
            dateFilter: "DATE_RANGE"
        }];

        $scope.selectedFilter = $scope.filters[0];

        $scope.selectFilter = function(value) {
            $scope.selectedFilter = $scope.filters[value];
            if (value !== 5) {
                $scope.refreshGrid();
            }
        };

        $scope.cardDateFormat = "dd-MM-yyyy";
        $scope.cardDateTimeFormat = "dd-MM-yyyy HH:mm";

        $scope.availableExportRecords = ['All','10', '25', '50', '100', '250'];
        $scope.availableExportFormats = ['pdf','xls', 'csv'];
        $scope.actualExportRecords = 'All';
        $scope.actualExportColumns = 'All';
        $scope.exportFormat = 'pdf';
        $scope.checkboxModel = {
            exportWithOrder : false,
            exportWithFilter : true
        };
        $scope.disableExportButton = false;
        $scope.exportTaskId = null;
        $scope.exportProgress = 0;
        $scope.exportStatusTimer = null;

        $scope.exportEntityInstances = function () {
            $scope.checkboxModel.exportWithFilter = true;
            $('#exportEvsMbararaInstanceModal').modal('show');
        };

        $scope.changeExportRecords = function (records) {
            $scope.actualExportRecords = records;
        };

        $scope.changeExportFormat = function (format) {
            $scope.exportFormat = format;
        };

        $scope.closeExportEvsMbararaInstanceModal = function () {
            $scope.cancelExport();

            $('#exportEvsMbararaInstanceForm').resetForm();
            $('#exportEvsMbararaInstanceModal').modal('hide');
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
            $scope.disableExportButton = false;
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

        $scope.exportInstanceWithUrl = function(url) {
            $scope.disableExportButton = true;
            $scope.exportProgress = 0;

            if ($scope.selectedLookup !== undefined && $scope.checkboxModel.exportWithFilter === true) {
                url = url + "&lookup=" + (($scope.selectedLookup) ? $scope.selectedLookup.lookupName : "");
                url = url + "&fields=" + encodeURIComponent(JSON.stringify($scope.lookupBy));
            }

            $http.get(url)
            .success(function (data) {
                $scope.exportTaskId = data;

                $scope.exportStatusTimer = setInterval(function(){$scope.checkExportStatus()}, 5000);
            })
            .error(function (response) {
                $scope.finishExport();
                handleResponse('mds.error', 'mds.error.exportData', response);
            });
        };

        $scope.parseDate = function(date, offset) {
            if (date !== undefined && date !== null) {
                var parts = date.split('-'), date;

                if (offset) {
                    date = new Date(parts[0], parts[1] - 1, parseInt(parts[2]) + offset);
                } else {
                    date = new Date(parts[0], parts[1] - 1, parts[2]);
                }
                return date;
            }
            return undefined;
        };

        $scope.lookupBy = {};
        $scope.selectedLookup = undefined;
        $scope.lookupFields = [];

        $scope.getLookups = function(url) {
            $scope.lookupBy = {};
            $scope.selectedLookup = undefined;
            $scope.lookupFields = [];

            $http.get(url)
            .success(function(data) {
                $scope.lookups = data;
            });
        };

        /**
        * Shows/Hides lookup dialog
        */
        $scope.showLookupDialog = function() {
            $("#lookup-dialog")
            .css({'top': ($("#lookupDialogButton").offset().top - $("#main-content").offset().top) - 40,
            'left': ($("#lookupDialogButton").offset().left - $("#main-content").offset().left) - 15})
            .toggle();
            $("div.arrow").css({'left': 50});
        };

        $scope.hideLookupDialog = function() {
            $("#lookup-dialog").hide();
        };

        /**
        * Marks passed lookup as selected. Sets fields that belong to the given lookup and resets lookupBy object
        * used to filter instances by given values
        */
        $scope.selectLookup = function(lookup) {
            $scope.selectedLookup = lookup;
            $scope.lookupFields = lookup.lookupFields;
            $scope.lookupBy = {};
        };

        /**
        * Removes lookup and resets all fields associated with a lookup
        */
        $scope.removeLookup = function() {
            $scope.lookupBy = {};
            $scope.selectedLookup = undefined;
            $scope.lookupFields = [];
            $scope.filterInstancesByLookup();
        };

        /**
        * Hides lookup dialog and sends signal to refresh the grid with new data
        */
        $scope.filterInstancesByLookup = function() {
            $scope.showLookupDialog();
            $scope.refreshGrid();
        };

        $scope.refreshGrid = function() {
            $scope.lookupRefresh = !$scope.lookupRefresh;
        };

        $scope.buildLookupFieldName = function (field) {
            if (field.relatedName !== undefined && field.relatedName !== '' && field.relatedName !== null) {
                return field.name + "." + field.relatedName;
            }
            return field.name;
        };

        /**
        * Depending on the field type, includes proper html file containing visual representation for
        * the object type. Radio input for boolean, select input for list and text input as default one.
        */
        $scope.loadInputForLookupField = function(field) {
            var value = "default", type = "field";

            if (field.className === "java.lang.Boolean") {
                value = "boolean";
            } else if (field.className === "java.util.Collection") {
                value = "list";
            } else if (field.className === "org.joda.time.DateTime" || field.className === "java.util.Date") {
                value = "datetime";
            } else if (field.className === "org.joda.time.LocalDate") {
                value = "date";
            }

            if ($scope.isRangedLookup(field)) {
                type = "range";
                if (!$scope.lookupBy[$scope.buildLookupFieldName(field)]) {
                    $scope.lookupBy[$scope.buildLookupFieldName(field)] = {min: '', max: ''};
                }
            } else if ($scope.isSetLookup(field)) {
                type = 'set';
                if (!$scope.lookupBy[$scope.buildLookupFieldName(field)]) {
                    $scope.lookupBy[$scope.buildLookupFieldName(field)] = [];
                }
            }

            return '../evs-mbarara/resources/partials/lookups/{0}-{1}.html'.format(type, value);
        };

        $scope.isRangedLookup = function(field) {
            return $scope.isLookupFieldOfType(field, 'RANGE');
        };

        $scope.isSetLookup = function(field) {
            return $scope.isLookupFieldOfType(field, 'SET');
        };

        $scope.isLookupFieldOfType = function(field, type) {
            var i, lookupField;
            for (i = 0; i < $scope.selectedLookup.lookupFields.length; i += 1) {
                lookupField = $scope.selectedLookup.lookupFields[i];
                if ($scope.buildLookupFieldName(lookupField) === $scope.buildLookupFieldName(field)) {
                    return lookupField.type === type;
                }
            }
        };

        $scope.getComboboxValues = function (settings) {
            var labelValues = MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.values'}], true).value, keys = [], key;
            // Check the user supplied flag, if true return string set
            if (MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.allowUserSupplied'}], true).value === true){
                return labelValues;
            } else {
                if (labelValues !== undefined && labelValues[0].indexOf(":") !== -1) {
                    labelValues =  $scope.getAndSplitComboboxValues(labelValues);
                    for(key in labelValues) {
                        keys.push(key);
                    }
                    return keys;
                } else {        // there is no colon, so we are dealing with a string set, not a map
                    return labelValues;
                }
            }
        };

        $scope.getComboboxDisplayName = function (settings, value) {
            var labelValues = MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.values'}], true).value;
            // Check the user supplied flag, if true return string set
            if (MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.allowUserSupplied'}], true).value === true){
                return value;
            } else {
                if (labelValues[0].indexOf(":") === -1) { // there is no colon, so we are dealing with a string set, not a map
                    return value;
                } else {
                    labelValues =  $scope.getAndSplitComboboxValues(labelValues);
                    return labelValues[value];
                }
            }

        };

        $scope.getAndSplitComboboxValues = function (labelValues) {
            var doublet, i, map = {};
            for (i = 0; i < labelValues.length; i += 1) {
                doublet = labelValues[i].split(":");
                map[doublet[0]] = doublet[1];
            }
            return map;
        };

        $scope.resizeGridHeight = function(gridId) {
            var intervalHeightResize, gap, tableHeight;
            clearInterval(intervalHeightResize);
            intervalHeightResize = setInterval( function () {
                if ($('.overrideJqgridTable').offset() !== undefined) {
                    gap = 1 + $('.overrideJqgridTable').offset().top - $('.inner-center .ui-layout-content').offset().top;
                    tableHeight = Math.floor($('.inner-center .ui-layout-content').height() - gap - $('.ui-jqgrid-pager').outerHeight() - $('.ui-jqgrid-hdiv').outerHeight());
                    $('#' + gridId).jqGrid("setGridHeight", tableHeight);
                }
                clearInterval(intervalHeightResize);
            }, 250);
         };

        $scope.resizeGridWidth = function(gridId) {
            var intervalWidthResize, tableWidth;
            clearInterval(intervalWidthResize);
            intervalWidthResize = setInterval( function () {
                tableWidth = $('.overrideJqgridTable').width();
                $('#' + gridId).jqGrid("setGridWidth", tableWidth);
                clearInterval(intervalWidthResize);
            }, 250);
        }
    });

    controllers.controller('EvsMbararaSettingsCtrl', function ($scope, $http, $timeout) {
        $scope.errors = [];
        $scope.messages = [];

        $scope.availableVisits = [];

        $scope.boostRelVisitsChanged = function(change) {
            var value;

            if (change.added) {
                value = change.added.text;
                $scope.config.boosterRelatedVisits.push(value);
            } else if (change.removed) {
                value = change.removed.text;
                $scope.config.boosterRelatedVisits.removeObject(value);
            }
        };

        $scope.subStudyVisitsChanged = function(change) {
            var value;

            if (change.added) {
                value = change.added.text;
                $scope.config.subStudyVisits.push(value);
            } else if (change.removed) {
                value = change.removed.text;
                $scope.config.subStudyVisits.removeObject(value);
            }
        };

        $http.get('../evs-mbarara/evs-mbarara-config')
            .success(function(response){
                var i;
                $scope.config = response;
                $scope.originalConfig = angular.copy($scope.config);

                $http.get('../evs-mbarara/availableVisits')
                    .success(function(response){
                        $scope.availableVisits = response;
                        $timeout(function() {
                            $('#boostRelVisits').select2('val', $scope.config.boosterRelatedVisits);
                            $('#subStudyVisits').select2('val', $scope.config.subStudyVisits);
                        }, 50);

                    })
                    .error(function(response) {
                        $scope.errors.push($scope.msg('evsMbarara.settings.enroll.disconVacCampaigns.error', response));
                    });
            })
            .error(function(response) {
                $scope.errors.push($scope.msg('evsMbarara.settings.noConfig', response));
            });

        $scope.reset = function () {
            $scope.config = angular.copy($scope.originalConfig);

            $('#boostRelVisits').select2('val', $scope.config.boosterRelatedVisits);
            $('#subStudyVisits').select2('val', $scope.config.subStudyVisits);
        };

        function hideMsgLater(index) {
            return $timeout(function() {
                $scope.messages.splice(index, 1);
            }, 5000);
        }

        $scope.submit = function () {
            $http.post('../evs-mbarara/evs-mbarara-config', $scope.config)
                .success(function (response) {
                    $scope.config = response;
                    $scope.originalConfig = angular.copy($scope.config);
                    var index = $scope.messages.push($scope.msg('evsMbarara.settings.saved'));
                    hideMsgLater(index-1);
                })
                .error (function (response) {
                    //todo: better than that!
                    handleWithStackTrace('evsMbarara.error.header', 'evsMbarara.error.body', response);
                });
        };
    });

    controllers.controller('EvsMbararaRescheduleCtrl', function ($scope, $http, $timeout, $filter) {
        $scope.getLookups("../evs-mbarara/getLookupsForVisitReschedule");

        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[5];
        $scope.visitForPrint = {};

        $scope.newForm = function() {
            $scope.form = {};
            $scope.form.dto = {};
        };

        $scope.setActualDateToCurrentDate = function () {
            $scope.form.dto.actualDate = $filter('date')(new Date(), "yyyy-MM-dd");
        };

        $scope.showPlannedDate = function () {
            var isActualDateEmpty = $scope.form.dto.actualDate === null || $scope.form.dto.actualDate === "" || $scope.form.dto.actualDate === undefined;
            return isActualDateEmpty;
        };

        $scope.clearActualDate = function () {
           motechConfirm("evsMbarara.visitReschedule.removeActualDate", "evsMbarara.confirm", function(confirmed) {
                   if (confirmed) {
                       $scope.form.dto.actualDate = null;
                       $timeout(function() {
                           $('#actualDateInput').trigger('change');
                       }, 100);
                   }
           })
        };

        $scope.showRescheduleModal = function(modalHeaderMessage, modalBodyMessage) {
            $timeout(function() {
            $scope.rescheduleModalHeader = modalHeaderMessage;
            $scope.rescheduleModalBody = modalBodyMessage;
            $('#visitRescheduleModal').modal('show');
            $scope.setDatePicker();
            }, 10);
        };

        $scope.getUploadSuccessMessageAndTogglePrintButton = function () {
            if ($scope.form.dto && $scope.form.dto.actualDate) {
                $scope.rescheduleModalBody = $scope.msg('evsMbarara.visitReschedule.actualDateUpdateSuccessful');
                $scope.diablePrint = true;
            } else {
                $scope.rescheduleModalBody = $scope.msg('evsMbarara.visitReschedule.plannedDateUpdateSuccessful');
                $scope.diablePrint = false;
            }
        };

        $scope.setDatePicker = function () {
            var plannedDate = $scope.parseDate($scope.form.dto.plannedDate);
            var minDate = $scope.form.dto.minDate;

            if (plannedDate && minDate && plannedDate < minDate) {
                minDate = plannedDate;
            }

            var plannedDateInput = $('#plannedDateInput');
            plannedDateInput.datepicker("setDate", plannedDate);
            plannedDateInput.datepicker('option', 'minDate', minDate);
            plannedDateInput.datepicker('option', 'maxDate', $scope.form.dto.maxDate);

            var actualDateInput = $('#actualDateInput');
            actualDateInput.datepicker('option', 'minDate', $scope.form.dto.minActualDate);
            actualDateInput.datepicker('option', 'maxDate', $scope.form.dto.maxActualDate);
            if ($scope.form.dto.actualDate) {
                var actualDate = $scope.parseDate($scope.form.dto.actualDate);
                actualDateInput.datepicker("setDate", actualDate);
            } else {
                actualDateInput.datepicker("setDate", null);
            }
        };

        $scope.saveVisitReschedule = function(ignoreLimitation) {
            function sendRequest() {
                $scope.getUploadSuccessMessageAndTogglePrintButton();
                $http.post('../evs-mbarara/saveVisitReschedule/' + ignoreLimitation, $scope.form.dto)
                    .success(function(data) {
                        if (data && (typeof(data) === 'string')) {
                            jConfirm($scope.msg('evsMbarara.visitReschedule.confirmMsg', data), $scope.msg('evsMbarara.visitReschedule.confirmTitle'),
                                function (response) {
                                    if (response) {
                                        $scope.saveVisitReschedule(true);
                                    }
                                });
                        } else {
                            $("#visitReschedule").trigger('reloadGrid');
                            $scope.visitForPrint = data;
                            $scope.form.dto = undefined;
                        }
                    })
                    .error(function(response) {
                        motechAlert('evsMbarara.visitReschedule.updateError', 'evsMbarara.error', response);
                    });
            }

            if (ignoreLimitation) {
                sendRequest();
            } else {
                var confirmMsg = "evsMbarara.visitReschedule.confirm.shouldSavePlannedDate";
                if ($scope.form.dto.actualDate !== ""
                    && $scope.form.dto.actualDate !== undefined
                    && $scope.form.dto.actualDate !== null) {
                    confirmMsg = "evsMbarara.visitReschedule.confirm.shouldSaveActualDate";
                }
                motechConfirm(confirmMsg, "evsMbarara.confirm",
                    function(confirmed) {
                        if (confirmed) {
                            var daysBetween = Math.round((new Date - $scope.parseDate($scope.form.dto.actualDate))/(1000*60*60*24));
                            if (daysBetween > 7) {
                                motechConfirm("evsMbarara.visitReschedule.confirm.shouldSaveOldActualDate", "evsMbarara.confirm",
                                    function(confirmed) {
                                    if (confirmed) {
                                        sendRequest();
                                    }
                                })
                            } else {
                                sendRequest();
                            }
                        }
                })
            }
        };

        $scope.formIsFilled = function() {
            return $scope.form
                && $scope.form.dto
                && ($scope.form.dto.actualDate || $scope.form.dto.plannedDate);
        };

        $scope.exportInstance = function() {
            var sortColumn, sortDirection, url = "../evs-mbarara/exportInstances/visitReschedule";
            url = url + "?outputFormat=" + $scope.exportFormat;
            url = url + "&exportRecords=" + $scope.actualExportRecords;

            if ($scope.checkboxModel.exportWithFilter === true) {
                url = url + "&dateFilter=" + $scope.selectedFilter.dateFilter;

                if ($scope.selectedFilter.startDate) {
                    url = url + "&startDate=" + $scope.selectedFilter.startDate;
                }

                if ($scope.selectedFilter.endDate) {
                    url = url + "&endDate=" + $scope.selectedFilter.endDate;
                }
            }

            if ($scope.checkboxModel.exportWithOrder === true) {
                sortColumn = $('#visitReschedule').getGridParam('sortname');
                sortDirection = $('#visitReschedule').getGridParam('sortorder');

                url = url + "&sortColumn=" + sortColumn;
                url = url + "&sortDirection=" + sortDirection;
            }

            $scope.exportInstanceWithUrl(url);
        };

        $scope.$watch('form.dto.ignoreDateLimitation', function (value) {
            if ($scope.form && $scope.form.dto) {
                var plannedDate = $scope.parseDate($scope.form.dto.plannedDate);
                var minDate = $scope.earliestDateToReturn;

                if (!value) {
                    $scope.form.dto.maxDate = $scope.latestDateToReturn;
                }

                if (plannedDate && minDate && plannedDate < minDate) {
                    minDate = plannedDate;
                }

                $scope.form.dto.minDate = minDate;
            }
        });

        $scope.setPrintData = function(document, participantId, plannedDate) {

            $('#versionDate', document).html($filter('date')(new Date(), $scope.cardDateTimeFormat));
            $('#subjectId', document).html(participantId);
            $('#date', document).html($filter('date')($scope.parseDate(plannedDate), $scope.cardDateFormat));
        };

        $scope.print = function() {

            setTimeout(function() {
                var subjectId = $scope.visitForPrint.participantId;
                var date = $scope.visitForPrint.plannedDate;

                var winPrint = window.open("../evs-mbarara/resources/partials/card/visitRescheduleCard.html");
                 if ((!(window.ActiveXObject) && "ActiveXObject" in window) || (navigator.userAgent.indexOf("MSIE") > -1)) {
                   // iexplorer
                    var windowOnload = winPrint.onload || function() {
                        setTimeout(function(){
                            $scope.setPrintData(winPrint.document, subjectId, date);
                            winPrint.focus();
                            winPrint.print();
                        }, 500);
                      };

                      winPrint.onload = new function() { windowOnload(); } ;
                 } else {
                    winPrint.onload = function() {
                        $scope.setPrintData(winPrint.document, subjectId, date);
                        winPrint.focus();
                        winPrint.print();
                    }
                 }
             }, 500);
        };
    });

    controllers.controller('EvsMbararaReportsCtrl', function ($scope, $routeParams) {
        $scope.reportType = $routeParams.reportType;
        $scope.reportName = "";

        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[0];

        $scope.buildColumnModel = function (colModel) {
            var newColModel = colModel;
            for (var i in colModel) {
                if(!colModel[i].hasOwnProperty('formatoptions') && colModel[i].hasOwnProperty('formatter')) {
                    newColModel[i].formatter = eval("(" + colModel[i].formatter + ")");
                }
            }
            return newColModel;
        };

        $scope.buildColumnNames = function (colNames) {
            var newColNames = colNames;
            for(var i in colNames) {
                newColNames[i] = $scope.msg(colNames[i]);
            }
            return newColNames;
        };

        var url;
        switch($scope.reportType){
            case "dailyClinicVisitScheduleReport":
                url = "../evs-mbarara/getLookupsForDailyClinicVisitScheduleReport";
                $scope.reportName = $scope.msg('evsMbarara.reports.dailyClinicVisitScheduleReport');
                break;
            case "followupsMissedClinicVisitsReport":
                url = "../evs-mbarara/getLookupsForFollowupsMissedClinicVisitsReport";
                $scope.reportName = $scope.msg('evsMbarara.reports.followupsMissedClinicVisitsReport');
                break;
            case "MandEMissedClinicVisitsReport":
                url = "../evs-mbarara/getLookupsForMandEMissedClinicVisitsReport";
                $scope.reportName = $scope.msg('evsMbarara.reports.MandEMissedClinicVisitsReport');
                break;
            case "ivrAndSmsStatisticReport":
                url = "../evs-mbarara/getLookupsForIvrAndSmsStatisticReport";
                $scope.reportName = $scope.msg('evsMbarara.reports.ivrAndSmsStatisticReport');
                break;
            case "optsOutOfMotechMessagesReport":
                url = "../evs-mbarara/getLookupsForOptsOutOfMotechMessagesReport";
                $scope.reportName = $scope.msg('evsMbarara.reports.optsOutOfMotechMessagesReport');
                break;
        }
        $scope.getLookups(url);

        $scope.exportInstance = function() {
            var url, rows, page, sortColumn, sortDirection;

            switch($scope.reportType){
                case "dailyClinicVisitScheduleReport":
                    url = "../evs-mbarara/exportDailyClinicVisitScheduleReport";
                    break;
                case "followupsMissedClinicVisitsReport":
                    url = "../evs-mbarara/exportFollowupsMissedClinicVisitsReport";
                    break;
                case "MandEMissedClinicVisitsReport":
                    url = "../evs-mbarara/exportMandEMissedClinicVisitsReport";
                    break;
                case "optsOutOfMotechMessagesReport":
                    url = "../evs-mbarara/exportOptsOutOfMotechMessagesReport";
                    break;
                case "ivrAndSmsStatisticReport":
                    url = "../evs-mbarara/exportIvrAndSmsStatisticReport";
                    break;
            }
            url = url + "?outputFormat=" + $scope.exportFormat;
            url = url + "&exportRecords=" + $scope.actualExportRecords;

            if ($scope.checkboxModel.exportWithOrder === true) {
                sortColumn = $('#reportTable').getGridParam('sortname');
                sortDirection = $('#reportTable').getGridParam('sortorder');

                url = url + "&sortColumn=" + sortColumn;
                url = url + "&sortDirection=" + sortDirection;
            }

            if ($scope.checkboxModel.exportWithFilter === true) {
                url = url + "&dateFilter=" + $scope.selectedFilter.dateFilter;

                if ($scope.selectedFilter.startDate) {
                    url = url + "&startDate=" + $scope.selectedFilter.startDate;
                }

                if ($scope.selectedFilter.endDate) {
                    url = url + "&endDate=" + $scope.selectedFilter.endDate;
                }
            }

            $scope.exportInstanceWithUrl(url);
        };

        $scope.backToEntityList = function() {
            window.location.replace('#/evsMbarara/reports');
        };
    });

    controllers.controller('EvsMbararaEnrollmentCtrl', function ($scope, $http) {
        var url = "../evs-mbarara/getLookupsForEnrollments";
        $scope.getLookups(url);

        $scope.enrollInProgress = false;

        $scope.refreshGrid = function() {
            $scope.lookupRefresh = !$scope.lookupRefresh;
        };

        $scope.refreshGridAndStayOnSamePage = function() {
            $scope.gridRefresh = !$scope.gridRefresh;
        };

        $scope.enroll = function(subjectId) {
            motechConfirm("evsMbarara.enrollSubject.ConfirmMsg", "evsMbarara.enrollSubject.ConfirmTitle",
              function (response) {
                  if (!response) {
                      return;
                  } else {
                      $scope.enrollInProgress = true;
                      $http.post('../evs-mbarara/enrollSubject', subjectId)
                        .success(function(response) {
                            motechAlert('evsMbarara.enrollment.enrollSubject.success', 'evsMbarara.enrollment.enrolledSubject');
                            $scope.refreshGridAndStayOnSamePage();
                            $scope.enrollInProgress = false;
                        })
                        .error(function(response) {
                            motechAlert('evsMbarara.enrollment.enrollSubject.error', 'evsMbarara.enrollment.error', response);
                            $scope.refreshGridAndStayOnSamePage();
                            $scope.enrollInProgress = false;
                        });
                  }
              });
        };

        $scope.unenroll = function(subjectId) {
            motechConfirm("evsMbarara.unenrollSubject.ConfirmMsg", "evsMbarara.unenrollSubject.ConfirmTitle",
              function (response) {
                  if (!response) {
                      return;
                  } else {
                      $scope.enrollInProgress = true;
                      $http.post('../evs-mbarara/unenrollSubject', subjectId)
                        .success(function(response) {
                            motechAlert('evsMbarara.enrollment.unenrollSubject.success', 'evsMbarara.enrollment.unenrolledSubject');
                            $scope.refreshGridAndStayOnSamePage();
                            $scope.enrollInProgress = false;
                        })
                        .error(function(response) {
                            motechAlert('evsMbarara.enrollment.unenrollSubject.error', 'evsMbarara.enrollment.error', response);
                            $scope.refreshGridAndStayOnSamePage();
                            $scope.enrollInProgress = false;
                        });
                  }
              });
        };

        $scope.goToAdvanced = function(subjectId) {
            $.ajax({
                url: '../evs-mbarara/checkAdvancedPermissions',
                success:  function(data) {
                    window.location.replace('#/evsMbarara/enrollmentAdvanced/' + subjectId);
                },
                async: false
            });
        };

        $scope.exportInstance = function() {
            var url, rows, page, sortColumn, sortDirection;

            url = "../evs-mbarara/exportSubjectEnrollment";
            url = url + "?outputFormat=" + $scope.exportFormat;
            url = url + "&exportRecords=" + $scope.actualExportRecords;

            if ($scope.checkboxModel.exportWithOrder === true) {
                sortColumn = $('#enrollmentTable').getGridParam('sortname');
                sortDirection = $('#enrollmentTable').getGridParam('sortorder');

                url = url + "&sortColumn=" + sortColumn;
                url = url + "&sortDirection=" + sortDirection;
            }

            $scope.exportInstanceWithUrl(url);
        };
    });

    controllers.controller('EvsMbararaEnrollmentAdvancedCtrl', function ($scope, $http, $timeout, $routeParams) {
        $scope.enrollInProgress = false;

        $scope.backToEnrolments = function() {
            window.location.replace('#/evsMbarara/enrollment');
        };

        $scope.selectedSubjectId = $routeParams.subjectId;

        $scope.refreshGrid = function() {
            $scope.lookupRefresh = !$scope.lookupRefresh;
        };

        $scope.enroll = function(campaignName) {
            $scope.enrollInProgress = true;
            $http.get('../evs-mbarara/enrollCampaign/' + $scope.selectedSubjectId + '/' + campaignName)
              .success(function(response) {
                  motechAlert('evsMbarara.enrollment.enrollSubject.success', 'evsMbarara.enrollment.enrolledSubject');
                  $scope.refreshGrid();
                  $scope.enrollInProgress = false;
              })
              .error(function(response) {
                  motechAlert('evsMbarara.enrollment.enrollSubject.error', 'evsMbarara.enrollment.error', response);
                  $scope.refreshGrid();
                  $scope.enrollInProgress = false;
              });
        };

        $scope.unenroll = function(campaignName) {
            $scope.enrollInProgress = true;
            $http.get('../evs-mbarara/unenrollCampaign/' + $scope.selectedSubjectId + '/' + campaignName)
              .success(function(response) {
                  motechAlert('evsMbarara.enrollment.unenrollSubject.success', 'evsMbarara.enrollment.unenrolledSubject');
                  $scope.refreshGrid();
                  $scope.enrollInProgress = false;
              })
              .error(function(response) {
                  motechAlert('evsMbarara.enrollment.unenrollSubject.error', 'evsMbarara.enrollment.error', response);
                  $scope.refreshGrid();
                  $scope.enrollInProgress = false;
              });
        };
    });

}());
