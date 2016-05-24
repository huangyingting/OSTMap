/**
 * Created by Christopher Rost on 24.04.2016.
 */

/**
 * The main angular instance with configurations.
 */
(function () {
    'use strict';

    angular.module('ostMapApp', [
            'ngRoute',
            'ui-leaflet',
            'nemLogging',
            'ui.layout',
            'ui.bootstrap',
            'angular-rickshaw'
        ])
        .config(defineRoutes);

    /**
     * Inject dependencies for the defineRoutes configuration
     * $routeProvider to set routes
     * @type {string[]}
     */
    defineRoutes.$inject = [
        '$routeProvider'
    ];

    /**
     * Sets the routes
     * align: 'left' or 'right'
     * @param $routeProvider
     */
    function defineRoutes($routeProvider) {
        $routeProvider
            .when('/list', {
                templateUrl: 'views/listView.html',
                controller: 'ListCtrl',
                name: 'List',
                align: 'left',
                glyphicon: 'glyphicon glyphicon-list'
            })
            .when('/map', {
                templateUrl: 'views/mapView.html',
                controller: 'MapCtrl',
                name: 'Map',
                align: 'left',
                glyphicon: 'glyphicon glyphicon-globe'
            })
            .when('/analytics', {
                templateUrl: 'views/analyticsView.html',
                controller: 'AnalyticsCtrl',
                name: 'Analytics',
                align: 'left',
                glyphicon: 'glyphicon glyphicon-stats'
            })
            .when('/about', {
                templateUrl: 'views/aboutView.html',
                controller: 'AboutCtrl',
                name: 'About',
                align: 'right',
                glyphicon: 'glyphicon glyphicon-info-sign'
            })
            .otherwise({redirectTo: '/list'});
    }
})();