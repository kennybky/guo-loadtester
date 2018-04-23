(function() {
    angular
        .module('app')
        .factory('chartLoader', chartLoader);

    function chartLoader() {
        var service = {
            renderFusionChart: renderFusionChart,
            updateFusionChart: updateFusionChart,
            renderBarChart : renderBarChart
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

        function generateColors(){
            let colors = []
            for (var i = 0; i < 20; i++){
                colors.push('#'+(Math.random()*0xFFFFFF<<0).toString(16));
            }
            return colors;
        }

        function renderBarChart(project){
            let data = project.stats
            let datum = []
            let colors = generateColors()
            let i = 0;
            data.forEach((stat)=>{
                let bar = {}
                bar.label = stat.url;
                bar.value = stat.responseTime;
                bar.color = colors[i]
                datum.push(bar);
                i++
            })
            FusionCharts.ready(function () {
                var revenueChart = new FusionCharts({
                    type: 'column2d',
                    renderAt: 'chart-container',
                    width: '550',
                    height: '350',
                    dataFormat: 'json',
                    dataSource: {
                        "chart": {
                            "caption": `Comparison of Response Times`,
                            "subCaption": `${project.title}`,
                            "xAxisName": "Urls",
                            "yAxisName": "Response Times (In Milliseconds)",
                            "paletteColors": "#0075c2",
                            "bgColor": "#ffffff",
                            "borderAlpha": "20",
                            "canvasBorderAlpha": "0",
                            "usePlotGradientColor": "0",
                            "plotBorderAlpha": "10",
                            "placevaluesInside": "1",
                            "rotatevalues": "1",
                            "valueFontColor": "#ffffff",
                            "showXAxisLine": "1",
                            "xAxisLineColor": "#999999",
                            "divlineColor": "#999999",
                            "divLineIsDashed": "1",
                            "showAlternateHGridColor": "0",
                            "subcaptionFontBold": "0",
                            "subcaptionFontSize": "14"
                        },
                        "data": datum,
                    }
                }).render();
            });
        }
    }
})();