(function() {
    angular
        .module('app')
        .factory('realTimeTracker', realTimeTracker);

    realTimeTracker.$inject = ['statusPromises', 'tester', 'chartTracker', '$interval', '$q', 'webtester'];

    function realTimeTracker(statusPromises, tester, chartTracker, $interval, $q, webtester) {
        var projectId;
        var project;

        var service = {
            setProjectId: setProjectId,
            startPinging: startPinging,
            setProject: setProject
        }

        return service;

        function setProjectId(id) {
            projectId = id;
        }

        function setProject(pj){
            project = pj
        }

        function startPinging() {
            var pingProm = $q.defer();
            google.charts.load('current', {'packages':['gauge']});
            google.charts.setOnLoadCallback(function() {
                pingPerformanceStatus(pingProm);
            });
            let pingPromise = pingProm.promise;
            pingPromise.then((response)=>{
                statusPromises.clear();
                console.log(response)
                webtester.save(project);

            })
            return pingProm
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
                    id: "performanceMonitorChart2",
                    type: 'realtimeline',
                    renderAt: 'performance-monitor-div',
                    width: '100%',
                    height: '400',
                    dataFormat: 'json',
                    dataSource: {
                        "chart": {
                            "caption": "Real-Time Performance",
                            "subCaption": `Time vs Avg response time(ms) for Upload size ${project.uploadSize}MB`,
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
                                let chartRef = FusionCharts("performanceMonitorChart2"),

                                    // We need to create a querystring format incremental update, containing
                                    // label in hh:mm:ss format
                                    currDate = new Date(),
                                    label = addLeadingZero(currDate.getHours()) + ":" +
                                        addLeadingZero(currDate.getMinutes()) + ":" +
                                        addLeadingZero(currDate.getSeconds()),
                                    // Build Data String in format &label=...&value=...
                                    strData = "";

                                if (avgPerf <=0 || avgPerf == null || avgPerf === undefined) {
                                    console.log(avgPerf)
                                    strData = "&label=" + label
                                        + "&value="
                                        + latestPerformance;
                                } else {
                                    console.log(avgPerf)
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

                webtester.start(project.title, project.url, project.method, project.parameters)
                    .then(function (res) {
                            let response = res.data;
                            latestPerformance = response.avgResponseTime;
                            avgPerf = response.cumAvgResponseTime;

                            //console.log(latestPerformance);
                            if (latestPerformance <= 1) {
                                options.max = 1
                            }
                            else if (latestPerformance >= options.max) {
                                options.max = latestPerformance * 2;
                            }
                            data.setValue(0, 1, latestPerformance);
                            performanceGauge.draw(data, options);
                            //updateData(latestPerformance, avgPerf)
                            saveData(latestPerformance);
                        },
                        (err)=>{
                            console.log(err)
                            pingProm.resolve(err);
                            statusPromises.clear();
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

        function saveData(avg){
            webtester.saveStats(project, avg).then((res)=>{
                if(project.cumAvg === 0 || project.cumAvg === undefined || project.cumAvg === null){
                    project.cumAvg = avg
                }
                let cumAvg = project.cumAvg;
                project.cumAvg = (cumAvg + avg)/2
            })
        }
    }
})();