(function () {
    'use strict';

    /* App Module */
    var evsMbarara = angular.module('evsMbarara', ['evsMbarara.controllers', 'evsMbarara.services',
          'evsMbarara.directives', 'motech-dashboard', 'data-services', 'ui.directives']), subjectId,
      callDetailRecordId, smsRecordId;

    $.ajax({
        url: '../mds/entities/getEntity/EVS Mbarara/Participant',
        success:  function(data) {
            subjectId = data.id;
        },
        async: false
    });

    $.ajax({
        url: '../mds/entities/getEntity/IVR Module/CallDetailRecord',
        success:  function(data) {
            callDetailRecordId = data.id;
        },
        async: false
    });

    $.ajax({
        url: '../mds/entities/getEntity/SMS Module/SmsRecord',
        success:  function(data) {
            smsRecordId = data.id;
        },
        async: false
    });

    $.ajax({
            url: '../evs-mbarara/available/evsTabs',
            success:  function(data) {
                evsMbarara.constant('EVS_MBARARA_AVAILABLE_TABS', data);
            },
            async:    false
        });

    evsMbarara.run(function ($rootScope, EVS_MBARARA_AVAILABLE_TABS) {
            $rootScope.EVS_MBARARA_AVAILABLE_TABS = EVS_MBARARA_AVAILABLE_TABS;
        });

    evsMbarara.config(function ($routeProvider, EVS_MBARARA_AVAILABLE_TABS) {

        var i, tab;

        for (i = 0; i < EVS_MBARARA_AVAILABLE_TABS.length; i = i + 1) {

            tab = EVS_MBARARA_AVAILABLE_TABS[i];

            if (tab === "subjects") {
                $routeProvider.when('/evsMbarara/{0}'.format(tab), {
                    templateUrl: '../evs-mbarara/resources/partials/evsMbararaInstances.html',
                    controller: 'MdsDataBrowserCtrl',
                    resolve: {
                        entityId: function ($route) {
                            $route.current.params.entityId = subjectId;
                        },
                        moduleName: function ($route) {
                            $route.current.params.moduleName = 'evs-mbarara';
                        }
                    }
                });
            } else if (tab === "reports") {
                $routeProvider
                  .when('/evsMbarara/reports', { templateUrl: '../evs-mbarara/resources/partials/reports.html' })
                  .when('/evsMbarara/reports/:reportType', { templateUrl: '../evs-mbarara/resources/partials/report.html', controller: 'EvsMbararaReportsCtrl' })
                  .when('/evsMbarara/callDetailRecord', { redirectTo: '/mds/dataBrowser/' + callDetailRecordId + '/evs-mbarara' })
                  .when('/evsMbarara/SMSLog', { redirectTo: '/mds/dataBrowser/' + smsRecordId + '/evs-mbarara' });
            } else if (tab === "enrollment") {
                $routeProvider
                  .when('/evsMbarara/enrollment', {templateUrl: '../evs-mbarara/resources/partials/enrollment.html', controller: 'EvsMbararaEnrollmentCtrl'})
                  .when('/evsMbarara/enrollmentAdvanced/:subjectId', {templateUrl: '../evs-mbarara/resources/partials/enrollmentAdvanced.html', controller: 'EvsMbararaEnrollmentAdvancedCtrl'});
            } else {
                $routeProvider.when('/evsMbarara/{0}'.format(tab),
                    {
                        templateUrl: '../evs-mbarara/resources/partials/{0}.html'.format(tab),
                        controller: 'EvsMbarara{0}Ctrl'.format(tab.capitalize())
                    }
                );
            }
        }

        $routeProvider
            .when('/evsMbarara/settings', {templateUrl: '../evs-mbarara/resources/partials/settings.html', controller: 'EvsMbararaSettingsCtrl'})
            .when('/evsMbarara/welcomeTab', { redirectTo: '/evsMbarara/' + EVS_MBARARA_AVAILABLE_TABS[0] });

    });
}());
