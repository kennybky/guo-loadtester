(function() {
    'use strict';
    angular.module('app').config(chartJsConfig);

    function chartJsConfig(ChartJsProvider) {
        // Configure all charts
        ChartJsProvider.setOptions({
            colours: ['#46BFBD'],
            responsive: true
        });
    }
})();
