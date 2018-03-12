(function() {
    angular
        .module('app')
        .controller('ExistingProjectController', ExistingProjectController);

    ExistingProjectController.$inject = [
        '$scope',
        '$anchorScroll',
        '$location',
        'projectData',
        'chartData',
        'chartLoader',
        'styler',
        'retester',
        'wizardProject',
        'tableItems',
        'chartOptions',
        'tester',
        'realTimePerformance',
        'statusPromises',
        'chartTracker',
        '$timeout'
    ];

    function ExistingProjectController($scope, $anchorScroll, $location, projectData, chartData, chartLoader, styler, retester, wizardProject,
                                        tableItems, chartOptions, tester, realTimePerformance, statusPromises, chartTracker, $timeout) {
        var vm = this;
        vm.act = '';
        vm.loadProjects = [];
        vm.scheduledProjects = [];
        vm.selected = selected;
        vm.selectAll = selectAll;
        vm.options = {
            minMode: 'month'
        };
        vm.modes = [{name: 'day'}, {name: 'month'}, {name: 'year'}];
        vm.getDatePicker = getDatePicker;
        vm.getAvailability = getAvailability;
        vm.getReliability = getReliability;
        vm.stopProject = stopProject;
        vm.selectProjectForAvail = selectProjectForAvail;
        vm.deleteProjects = deleteProjects;
        vm.clearAvailability = clearAvailability;
        vm.graphOverride = [
            {
                label: "Bar Chart",
                borderWidth: 1,
                type: 'bar'

            },
            {
                label: "Line Chart",
                borderWidth: 3,
                type: 'line'
            }
        ];
        vm.load = load;
        vm.retest = retest;
        vm.compare = compare;
        vm.projectAction = {};
        var timeOuts = {};

        $scope.$on('$destroy', function () {
            chartTracker.clearGoogleCharts();
            chartTracker.clearFusionCharts();
            for (timeo in timeOuts) $timeout.cancel(timeo);
            statusPromises.clear();
        });
        
        function activate() {
            projectData.getProjects().then(function (projects) {
                vm.loadProjects = projects[0];
                vm.scheduledProjects = projects[1];
                vm.dataFetched = true;
            });
        }

        activate();

        /**
         * Updates the button between load and compare depending on the
         * number of projects selected.
         */
        function selected(projects, type) {
            vm.allSelected = false;
            if (type === 'load') {
                vm.act = projectData.updateLoadBtn(projects);
            }
        }

        /**
         * Used to trigger the selection of all projects if the top check-box
         * is selected. Also updates the load or compare button accordingly.
         * @param allSelected {boolean} True or false indicating if all projects selected or not
         */
        function selectAll(projects, allSelected, type) {
            tableItems.selectAll(projects, allSelected);
            if (type === 'load') {
                vm.act = projectData.updateLoadBtn(projects);
            }
        }

        function getDatePicker(mode) {
            vm.options.minMode = mode.name;
        }


        function getAvailability(mode, date) {
            $timeout.cancel(timeOuts.datePickerMsg);
            vm.datePickerMsg = {};
            if (!mode || !date) {
                vm.datePickerMsg.class = styler.styleResult(false);
                vm.datePickerMsg.message = "You must select a mode and date.";
                timeOuts.datePickerMsg = $timeout(function() {
                    vm.datePickerMsg.message = undefined;
                }, 3000);
            }
            else {
                projectData.getAvailability(mode, date, vm.projectAvailability.projectid).then(function (response) {
                    vm.datePickerMsg.class = styler.styleResult(response.running);
                    if (response.running) {
                        switch (vm.options.minMode) {
                            case 'day':
                                vm.datePickerMsg.message = "Availability for " + date.getFullYear() + '-' +
                                    (date.getMonth() + 1) + '-' + date.getDate() + ': ' + response.message;
                                break;
                            case 'month':
                                vm.datePickerMsg.message = "Availability for " + date.getFullYear() + '-' +
                                    (date.getMonth() + 1) + ': ' + response.message;
                                break;
                            case 'year':
                                vm.datePickerMsg.message = "Availability for " + date.getFullYear() + ': ' + response.message;
                                break;
                        }
                    } else {
                        vm.datePickerMsg.message = response.message;
                    }
                });
            }
        }

        function clearAvailability() {
            vm.datePickerMsg.message = null;
        }

        function getReliability(project) {
            $timeout.cancel(timeOuts.reliabilityAction);
            vm.reliabilityAction = {};
            vm.projectReliability = project;
            projectData.getReliability(project.projectid).then(function (response) {
                vm.reliabilityAction.class = styler.styleResult(response.running);
                vm.reliabilityAction.message = response.message;
            });
        }

        function stopProject(project) {
            $timeout.cancel(timeOuts.projectAction);
            tester.stop(project.projectid).then(function (response) {
                projectMsg(response.running, response.message);
                if (!response.running) {
                    if (project.testType === 'performance') {
                        statusPromises.clear();
                    }
                    activate();
                }
            });
        }

        function selectProjectForAvail(project) {
            vm.projectAvailability = project;
        }

        /**
         * Delete the selected projects when the delete button is pressed.
         */
        function deleteProjects(projects) {
            $timeout.cancel(timeOuts.projectAction);
            if (vm.allSelected) {
                vm.allSelected = false;
            }
            var selectedProjects = tableItems.getSelectedItems(projects);
            if (selectedProjects.length == 0) {
                projectMsg(false, "You haven't selected any projects to delete.");
            } else {
                projectData.deleteProjects(projectData.filterNIP(selectedProjects)).then(function (response) {
                    activate();
                    projectMsg(true, response.data);
                }, function(error) {
                    projectMsg(false, error.data);
                });
            }
        }

        /**
         * Load charts for selected project.
         */
        function load() {
            vm.showLoadingBall = true;
            vm.projectLoaded = tableItems.getSelectedItem(vm.loadProjects);
            if (vm.projectLoaded) {
                statusPromises.clear();
                chartTracker.clearGoogleCharts();
                chartTracker.clearFusionCharts();
                $anchorScroll('chart-section');
                var type = vm.projectLoaded.testType;
                if (type == 'performance') {
                    if (vm.projectLoaded.inProgress) {
                        realTimePerformance.setProjectId(vm.projectLoaded.projectid);
                        realTimePerformance.startPinging(vm);
                    }
                    chartData.getChartData(vm.projectLoaded.uri, vm.projectLoaded.projectid, "performanceHistory").then(function(response) {
                        chartLoader.renderFusionChart(response.fusionChart);
                        vm.showLoadingBall = false;
                    });
                } else if (type == 'scalability' || 'capacity') {
                    // requests input, either in a uniform, normal, or poisson distributed manner.
                    chartData.getChartData(vm.projectLoaded.uri, vm.projectLoaded.projectid, "requestsGraph").then(function (response) {
                        // console.log(JSON.stringify(response));
                        vm.requestsGraphLabels = response.chart.labels;
                        vm.requestsGraphData = [response.chart.data, response.chart.data];
                        vm.requestsGraphOptions = chartOptions.requestGraphOptions("Request input: " + vm.projectLoaded.uri);
                    });

                    // Performance Scalability
                    chartData.getChartData(vm.projectLoaded.uri, vm.projectLoaded.projectid, "performanceScalabilityGraph").then(function (response) {
                        // console.log(JSON.stringify(response));
                        vm.performanceScalabilityLabels = response.chart.labels;
                        vm.performanceScalabilityData = [response.chart.data, response.chart.data];
                        //some misc message or info about the data. optional.
                        vm.performanceScalabilityOptions = chartOptions.performanceScalabilityOptions("Performance Scalability: " + vm.projectLoaded.uri);
                    });

                    // Success Scalability
                    chartData.getChartData(vm.projectLoaded.uri, vm.projectLoaded.projectid, "successScalabilityGraph").then(function (response) {
                        // console.log(JSON.stringify(response));
                        vm.successScalabilityLabels = response.chart.labels;
                        vm.successScalabilityData = [response.chart.data, response.chart.data];
                        vm.successScalabilityOptions = chartOptions.successScalabilityOptions("Success Scalability: " + vm.projectLoaded.uri);
                    });
                    vm.showLoadingBall = false;
                }
            }
        }

        /**
         * For each project selected, compare its name with those in the list
         *     projects in the database. If there is a match, generate the series
         *     and category object for that project URL. Once this is done for each
         *     project selected, generate the chart.
         */
        function compare() {
            alert("This functionality has not been implemented yet.");
            // vm.compareProjectsResults = [];
            // vm.chartsLoaded = false;
            // vm.compareChartsLoaded = true;
            // var projectsCompared = [];
            //
            // vm.loadProjects.forEach(function (project) {
            //     if (project.selected) {
            //         var projectName = project.projectname;
            //         projectsCompared.push(projectName);
            //         vm.compareProjectsResults.push({
            //             projectName: project.projectname,
            //             maxConnectionCt: project.maxConnCount,
            //             avgRspTime: project.avgResponseTime
            //         });
            //     }
            // });
            //
            // var projectList = projectsCompared.join(";");
            // chartData.getCompareData(projectList).then(function (compareData) {
            //     chartLoader.updateFusionChart(compareData, "compareProjectsChart", "msarea");
            // });
            //
            // chartData.getStatsData(projectList, "avg").then(function (compareData) {
            //     chartLoader.updateFusionChart(compareData, "avgProjectsChart", "bar2d");
            // });
            //
            // chartData.getStatsData(projectList, "min").then(function (compareData) {
            //     chartLoader.updateFusionChart(compareData, "minProjectsChart", "bar2d");
            // });
            //
            // chartData.getStatsData(projectList, "max").then(function (compareData) {
            //     chartLoader.updateFusionChart(compareData, "maxProjectsChart", "bar2d");
            // });
            //
            // chartData.getStatsData(projectList, "capacity").then(function (compareData) {
            //     chartLoader.updateFusionChart(compareData, "capacityProjectsChart", "bar2d");
            // });
            //
            // chartData.getStatsData(projectList, "reliability").then(function (compareData) {
            //     chartLoader.updateFusionChart(compareData, "reliabilityProjectsChart", "bar2d");
            // });
            
        }

        /**
         * Allows user to re-test a saved load test project. It'll
         * navigate to the new project page and configure the wizard
         * depending on if a project is a capacity or scalability test.
         */
        function retest(projects) {
            $timeout.cancel(timeOuts.projectAction);
            var selected = tableItems.getSelectedItem(projects);
            if (selected) {
                if (!(tableItems.getSelectedItems(projects).length === 1)) {
                    projectMsg(false, "Cannot select more than one project for retesting.");
                } else if (selected.inProgress) {
                    projectMsg(false, "Cannot re-test a project that's already running.");
                } else {
                    var options = {};
                    if (selected.testType === 'capacity' || selected.testType === 'scalability') {
                        options = {
                            userCount: selected.maxConcurrentUsers,
                            warmUpTime: selected.warmUpTime,
                            testTime: selected.testDuration,
                            failuresPermitted: selected.failuresPermitted,
                            stepDuration: selected.stepDuration,
                            stepCount: selected.stepCount
                        };
                        if (selected.testType == 'scalability') options.distribution = selected.distribution;
                    } else if (selected.testType == 'scheduled') {
                        selected.testType = 'availability';
                        options = {
                            timeUnits: 'Seconds',
                            timeout: 500,
                            interval: projectData.msToSec(selected.scheduleInterval)
                        };
                    }
                    var project = new wizardProject(selected.projectname, selected.testType, options, selected.uri);
                    retester.setProject(project);
                    $location.path('#/new-project');
                }
            }
            else {
                projectMsg(false, "Select exactly one project to retest.");
            }
        }

        function projectMsg(running, message) {
            vm.projectAction.class = styler.styleResult(running);
            vm.projectAction.message = message;
            timeOuts.projectAction = $timeout(function() {
                vm.projectAction.message = undefined;
            }, 3000);
        }
    }
})();

