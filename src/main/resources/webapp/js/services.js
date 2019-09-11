(function () {
    'use strict';

    /* Services */

    var services = angular.module('evsMbarara.services', ['ngResource']);

    services.factory('AllParticipants', function($resource) {
        return $resource('../evs-mbarara/participants/all', {}, {});
    });

}());
