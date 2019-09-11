(function () {
    'use strict';

    /* Services */

    var services = angular.module('evsMbarara.services', ['ngResource']);

    services.factory('Screenings', function($resource) {
        return $resource('../evs-mbarara/screenings', {}, {
            'get': {url: '../evs-mbarara/screenings/:id', method: 'GET'}
        });
    });

    services.factory('Clinics', function($resource) {
        return $resource('../evs-mbarara/clinics', {}, {});
    });

    services.factory('ScreenedParticipants', function($resource) {
        return $resource('../evs-mbarara/participants/screened', {}, {});
    });

}());
