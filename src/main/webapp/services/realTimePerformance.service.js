(function() {
    angular
        .module('app')
        .factory('realTimePerformance', realTimePerformance);

    realTimePerformance.$inject = ['statusPromises', 'tester', 'chartTracker', '$interval', '$q'];

    function realTimePerformance(statusPromises, tester, chartTracker, $interval, $q) {
        var projectId;

        var service = {
            setProjectId: setProjectId,
            startPinging: startPinging
        }

        return service;

        function setProjectId(id) {
            projectId = id;
        }

        function startPinging() {
            var pingProm = $q.defer();
            google.charts.load('current', {'packages':['gauge']});
            google.charts.setOnLoadCallback(function() {
                pingPerformanceStatus(pingProm);
            });
            return pingProm.promise;
        }

        function pingPerformanceStatus (pingProm) {
            var latestPerformance = 0;
            var avgPerf = 0;
            chartTracker.clearFusionCharts();
            chartTracker.clearGoogleCharts();
            statusPromises.clear();
            var data = google.visualization.arrayToDataTable([
                ['Label', 'Value'],
                ['Resp Time (ms)', 0]
            ]);

            var options = {
                minorTicks: 10,
                max: 100
            };

            FusionCharts.ready(function () {
                var performanceChartMon = new FusionCharts({
                    id: "performanceMonitorChart",
                    type: 'realtimeline',
                    renderAt: 'performance-monitor-div',
                    width: '100%',
                    height: '400',
                    dataFormat: 'json',
                    dataSource: {
                        "chart": {
                            "caption": "Real-Time Performance",
                            "subCaption": "Time vs Avg response time(ms)",
                            "xAxisName": "Time",
                            "yAxisName": "Avg Response Time (ms)",
                            "refreshinterval": "1",
                            "numdisplaysets": "10",
                            "labeldisplay": "rotate",
                            "showValues": "1",
                            "scrollheight": "10",
                            "flatScrollBars": "1",
                            "scrollShowButtons": "0",
                            "scrollColor": "#cccccc",
                            "showRealTimeValue": "1",
                            "theme": "fint"
                        },
                        "categories": [{
                            "category": [{}]
                        }],
                        "dataset": [{
                        	"seriesname": "current",
                            "showvalues": "1",
                            "color" : "#0546af",
                        	"data": [{}]
                        },
                        {
                        "seriesname": "avg",
                        "showvalues": "1",
                        "color":"#46af5b",
                    	"data": [{}]
                    }
                        ]
                    },
                    "events": {
                        "initialized": function (e) {
                            function addLeadingZero(num){
                                return (num <= 9)? ("0"+num) : num;
                            }
                            function updateData() {
                                // Get reference to the chart using its ID
                                var chartRef = FusionCharts("performanceMonitorChart"),
                                // We need to create a querystring format incremental update, containing
                                // label in hh:mm:ss format
                                    currDate = new Date(),
                                    label = addLeadingZero(currDate.getHours()) + ":" +
                                        addLeadingZero(currDate.getMinutes()) + ":" +
                                        addLeadingZero(currDate.getSeconds()),
                                // Build Data String in format &label=...&value=...
                                    strData = "";
                                	if (avgPerf == -1) {
                                		console.log(avgPerf)
                                		strData = "&label=" + label
                                        + "&value="
                                        + latestPerformance;
                                	} else {
                                    strData = "&label=" + label
                                        + "&value="
                                        + latestPerformance + "|" + avgPerf;
                                	}
                                // Feed it to chart.
                                chartRef.feedData(strData);
                            }

                            var monitorPromise = $interval(function () {
                                updateData();
                            }, 1000);
                            statusPromises.add(monitorPromise);
                        }
                    }
                }).render();
                chartTracker.addFusionChart(performanceChartMon);
            });


            /* 10 / 10 = 1, 20 / 10 = 2, ... 100 / 10 = 10 , 110 / 10 = 11,/*/
            var performanceGauge = new google.visualization.Gauge(document.getElementById('performance-gauge-div'));

            performanceGauge.draw(data, options);

            chartTracker.addGoogleChart(performanceGauge);
            var gaugePromise = $interval(function() {
                tester.getPerformanceStatus(projectId).then(function(response) {
                    if (response.running) {
                        latestPerformance = response.avgResponseTime;
                        avgPerf = response.cumAvgResponseTime;
                        // console.log(response.avgResponseTime);
                        // console.log(latestPerformance);
                        if (latestPerformance <= 1) {
                            options.max = 1
                        }
                        else if (latestPerformance >= options.max) {
                            options.max = latestPerformance * 2;
                        }
                        data.setValue(0, 1, latestPerformance);
                        performanceGauge.draw(data, options);
                    } else {
                        pingProm.resolve(response);
                        statusPromises.clear();
                    }
                });
            }, 500); //check for real time update every 500 ms.
            statusPromises.add(gaugePromise);

            function resizeGauge () {
                console.log('Resize gauge called.');
                performanceGauge.draw(data, options);
            }
            if (document.addEventListener) {
                window.addEventListener('resize', resizeGauge);
            }
            else if (document.attachEvent) {
                window.attachEvent('onresize', resizeGauge);
            }
            else {
                window.resize = resizeGauge;
            }
        }
    }
})();