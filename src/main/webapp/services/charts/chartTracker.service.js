(function () {
    angular
        .module('app')
        .factory('chartTracker', chartTracker);

    function chartTracker() {
        var fusionCharts = [];
        var googleCharts = [];

        var service = {
            addGoogleChart: addGoogleChart,
            addFusionChart: addFusionChart,
            clearFusionCharts: clearFusionCharts,
            clearGoogleCharts: clearGoogleCharts
        }

        return service;

        function addGoogleChart(googleChart) {
            googleCharts.push(googleChart);
        }

        function addFusionChart(fusionChart) {
            fusionCharts.push(fusionChart);
        }

        function clearFusionCharts() {
            fusionCharts.forEach(function(fusionChart) {
                var index = fusionCharts.indexOf(fusionChart);
                if (index > -1) {
                    fusionCharts.splice(index, 1);
                    fusionChart.dispose();
                }
            });
        }

        function clearGoogleCharts() {
            googleCharts.forEach(function(googleChart) {
                var index = googleCharts.indexOf(googleChart);
                if (index > -1) {
                    googleCharts.splice(index, 1);
                    googleChart.clearChart();
                }
            });
        }
    }
})();