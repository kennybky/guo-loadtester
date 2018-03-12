(function() {
    angular
        .module('app')
        .factory('chartLoader', chartLoader);

    function chartLoader() {
        var service = {
            renderFusionChart: renderFusionChart,
            updateFusionChart: updateFusionChart
        }

        return service;

        function renderFusionChart(data) {
            FusionCharts.ready(function () {
                var fusionChart = new FusionCharts(data);
                fusionChart.render();
            });
        }

        function updateFusionChart(data, chartType, plotType) {
            var seriesObject;
            if (typeof data.series === 'undefined' || data.series === null) {
                seriesObject = data.dataset;
            } else {
                seriesObject = data.series;
                seriesObject.seriesname = data.uri;
            }

            var categoryObject = data.category;

            var seriesArray;
            if (seriesObject.constructor === Array) {
                seriesArray = seriesObject;
            } else {
                seriesArray = [seriesObject];
            }

            $("#" + chartType).insertFusionCharts({
                type: plotType,
                renderAt: 'chart-container',
                width: '100%',
                height: '550',
                dataFormat: 'json',
                dataSource: {
                    "chart": data.chart,
                    "categories": [categoryObject],
                    "dataset": seriesArray
                }
            });
        }
    }
})();