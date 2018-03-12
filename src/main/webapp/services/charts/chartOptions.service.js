(function() {
    angular
        .module('app')
        .factory('chartOptions', chartOptions);

    function chartOptions() {
        var service = {
            requestGraphOptions: requestGraphOptions,
            performanceScalabilityOptions: performanceScalabilityOptions,
            successScalabilityOptions: successScalabilityOptions
        };

        return service;

        function requestGraphOptions(title) {
            return {
                scales: {
                    yAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'Number of Requests'
                        }
                    }],
                    xAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'Time (ms)'
                        }
                    }]
                },
                tooltips: {
                    callbacks: {
                        label: function (tooltipItem, data) {
                            var data = data.datasets[0].data; //array of data objects.
                            var toolTipDataInd = tooltipItem.index;
                            return "Number of requests: " + data[toolTipDataInd];
                        }
                    }
                },
                title: {
                    display: true,
                    text: title,
                    fullWidth: true
                }
            };
        }

        function performanceScalabilityOptions(title) {
            return {
                scales: {
                    yAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'Average Response Time'
                        }
                    }],
                    xAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'Time (ms)'
                        }
                    }]
                },
                tooltips: {
                    callbacks: {
                        label: function (tooltipItem, data) {
                            var data = data.datasets[0].data; //array of data objects.
                            var toolTipDataInd = tooltipItem.index;
                            return "Avg Response Time: " + data[toolTipDataInd] + " ms";
                        }
                    }
                },
                title: {
                    display: true,
                    text: title,
                    fullWidth: true
                }
            };
        }

        function successScalabilityOptions(title) {
            return {
                scales: {
                    yAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'Number of Status Code 200 Requests'
                        }
                    }],
                    xAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'Time (ms)'
                        }
                    }]
                },
                tooltips: {
                    callbacks: {
                        label: function (tooltipItem, data) {
                            var data = data.datasets[0].data; //array of data objects.
                            var toolTipDataInd = tooltipItem.index;
                            return "Status code 200 requests: " + data[toolTipDataInd];
                        }
                    }
                },
                title: {
                    display: true,
                    text: title,
                    fullWidth: true
                }
            };
        }
    }
})();