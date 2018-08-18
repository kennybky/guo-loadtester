(function() {
    angular
        .module('app')
        .controller('NewProjectController', NewProjectController);

    NewProjectController.$inject = [
        '$scope',
        '$anchorScroll',
        '$interval',
        '$q',
        '$timeout',
        'retester',
        'tester',
        'webServices',
        'styler',
        'wizardProject',
        'WizardHandler',
        'urlBuilder',
        'chartOptions',
        'statusPromises',
        'chartTracker',
        'realTimePerformance'
    ];

    function NewProjectController($scope, $anchorScroll, $interval, $q, $timeout, retester, tester, webServices,
    styler, wizardProject, WizardHandler, urlBuilder, chartOptions, statusPromises, chartTracker, realTimePerformance) {
        var vm = this;
        vm.services = [];
        vm.names = [];
        vm.savedUrls = [];
        vm.testTypes = ['availability', 'capacity', 'performance', 'reliability', 'scalability'];
        vm.validation = {};
        vm.resetConfiguredUrl = resetConfiguredUrl;
        vm.nameValidation = nameValidation;
        vm.serviceConfigValidation = serviceConfigValidation;
        vm.testTypeValidation = testTypeValidation;
        vm.saveUrl = saveUrl;
        vm.initTest = initTest;
        vm.stop = stop;
        vm.scrollToCharts = scrollToCharts;
        vm.getMethods = getMethods;
        vm.methods = [];
        vm.getParams = getParams;
        vm.params = [];
        vm.updateParams = updateParams;
        vm.initProjectOptions = initProjectOptions;
        var timeOuts = {};
        var skipValidation = false;
        vm.webUrl = "";
        vm.typeSelected = "GET";
        vm.paramName = ""
        vm.paramValue = "";
        vm.webParams = [];
        vm.addWebParam = addWebParam;
        vm.removeWebParam = removeWebParam;
        vm.validUrl = validUrl;
        vm.urlValid = true;
        vm.saveWebUrl = saveWebUrl;
        vm.constructUrl = constructUrl;


        /**
         * Prior to this controller's scope being destroyed (e.g. navigating to a different view),
         * execute the callback function.
         */
        $scope.$on('$destroy', function () {
            retester.setProject(null);
            retester.setService(null);
            for (timeo in timeOuts) $timeout.cancel(timeo);
            chartTracker.clearGoogleCharts();
            chartTracker.clearFusionCharts();
            statusPromises.clear();
        });

        activate();

        function getSavedUrls() {
            webServices.getSavedUrls().then(function (savedUrls) {
                vm.savedUrls = savedUrls;
            });
        }

        function activate() {
            /**
             * Get the services from the backend
             */
            webServices.getServices().then(function (services) {
                vm.services = services.servicesWithBuilder;
                vm.names = webServices.serviceDisplays(vm.services);

                var serviceRetest = retester.getService();
                if (serviceRetest) {
                    if (serviceRetest.baseUri) {
                        vm.urlSelectType = 'Url Builder';
                        vm.project = new wizardProject("default", null, null, null, "GET");
                        vm.serviceName = serviceRetest.name;
                        getMethods(vm.serviceName);
                    } else {
                        vm.urlSelectType = 'Saved Test Urls';
                        vm.project = new wizardProject("default", null, null, serviceRetest.testUri, "GET");
                        vm.savedUrl = serviceRetest.testUri;
                    }
                }
            });
            getSavedUrls();

            vm.project = retester.getProject();
            console.log(vm.project)

            /**
             * If there's a project to retester, go to the last step in the Wizard.
             */
            if (vm.project && vm.project.options) {
                console.log("yuh")
                skipValidation = true;
                $scope.$watch(
                    function () {
                        return WizardHandler.wizard();
                    },
                    function (wizard) {
                        if (wizard) wizard.goTo("Review & Start Testing");
                    });
            }
        }

        function resetConfiguredUrl() {
            vm.savedUrl = undefined;
        }

        function nameValidation() {
            if (skipValidation) {
                skipValidation = false;
                return true;
            }
            if (!vm.validation.name) {
                return false;
            } else {
                $timeout.cancel(timeOuts.nameValidation);
                var d = $q.defer();
                tester.validateProjectName(vm.validation.name).then(function (response) {
                    vm.validateName = {};
                    vm.validateName.class = styler.styleResult(response.valid);
                    if (!response.valid) {
                        vm.validateName.message = response.message;
                        timeOuts.nameValidation = $timeout(function () {
                            vm.validateName.message = undefined;
                        }, 3000);
                    } else {
                        createProject(vm.validation.name);
                    }
                    return d.resolve(response.valid);
                });
                return d.promise;
            }
        }

        /**
         * Create a new project object if one d oesn't yet exist.
         * @param name
         */
        function createProject(name) {
            if (vm.project) {
                vm.project.name = name;
            } else {
                vm.project = new wizardProject(name);
            }
        }

        function saveUrl(uri, selectType) {
            $timeout.cancel(timeOuts.saveUrl);
            var saveUrlProm = $q.defer();
            vm.savedUrlAction = {};
            if (selectType === 'Url Builder' && urlBuilder.getMethod()) {
                webServices.saveTestUrl(uri, urlBuilder.getMethod().id).then(function (success) {
                    saveUrlProm.resolve(success);
                }, function(error) {
                    saveUrlProm.reject(error);
                });
            } else {
                webServices.saveTestUrl(uri).then(function (success) {
                    saveUrlProm.resolve(success);
                }, function(error) {
                    saveUrlProm.reject(error);
                });
            }
            var message;
            saveUrlProm.promise.then(function(response) {
                message = response.data;
                getSavedUrls();
                vm.savedUrlAction.class = styler.styleResult(true);
            }).catch(function(response) {
                message = response.data;
                vm.savedUrlAction.class = styler.styleResult(false);
            }).finally(function() {
                vm.savedUrlAction.message = message;
                timeOuts.saveUrl = $timeout(function() {
                    vm.savedUrlAction.message = undefined;
                }, 3000);
            });
        }

        function saveWebUrl() {
            if(vm.validUrl()) {
                vm.urlValid = true;
                vm.project.uri = vm.webUrl;
                vm.project.params = vm.webParams;
                vm.project.uri = vm.constructUrl(vm.project.uri, vm.webParams)
            } else {
                vm.urlValid = false;
            }
        }

        function constructUrl(uri, params) {
            let first = true;
            params.forEach((param)=>{
                if(first){
                    first = false;
                    uri +="?"
                } else {
                    uri +="&"
                }
                uri+= `${param.key}=${param.value}`
            })

            return uri;
        }

    function validUrl() {
            if (vm.webUrl === null || vm.webUrl === undefined || vm.webUrl === "")
            {return false;}
            else return !(vm.webUrl.indexOf("localhost:") === -1 && vm.webUrl.indexOf("https://") === -1);
    }




        function addWebParam(name, value){
            let param = {};
            param.key = name;
            param.value = value;
            vm.webParams.push(param);
        }

        function removeWebParam(id) {
            vm.webParams = vm.webParams.filter((obj) => {
                return obj.key !== id
            });
        }

        /**
         * Make a call to the back end to initiate a load test. If back end
         * returns "RUNNING", then a test has been successfully intiated, and
         * we'll call vm.start to constantly get test statuses.
         */
        function initTest() {
            vm.loadTestMessage = null;
            console.log(vm.project)
            console.log("yuh")
            vm.RTPERF = false;
            $timeout.cancel(timeOuts.initTest);
            var initTestProm = $q.defer();
            vm.initTestAction = {};
            switch (vm.project.type) {
                case 'performance':
                    tester.start(
                        vm.project).then(function (response) {
                        initTestProm.resolve(response);
                        if (response.running) {
                            realTimePerformance.setProjectId(response.projectid);
                            var pingProm = realTimePerformance.startPinging();
                            pingProm.then(function(response) {
                                $timeout.cancel(timeOuts.initTest);
                                testProgMsg(response.running, response.message);
                            });
                            vm.RTPERF = true;
                        }
                    });
                    break;
                case 'capacity':
                    tester.start(
                        vm.project).then(function (response) {
                        initTestProm.resolve(response);
                        if (response.running) {
                            pingCapacityStatus(response.uri, response.projectid);
                        }
                    });
                    break;
                case 'scalability':
                    tester.start(
                        vm.project).then(function (response) {
                        initTestProm.resolve(response);
                        if (response.running) {
                            pingScalabilityStatus(response.uri, response.projectid);
                        }
                    });
                    break;
                case 'reliability':
                case 'availability':
                    tester.start(vm.project).then(function (response) {
                        initTestProm.resolve(response);
                        if (response.running) {
                            statusPromises.clear();
                            chartTracker.clearFusionCharts();
                            chartTracker.clearGoogleCharts();
                        }
                    });
                    break;
                default:
                    var response = {projectid: -1, running: false, message: "Invalid or empty test type."};
                    initTestProm.resolve(response);
                    break;
            }
            initTestProm.promise.then(function(response) {
                if (response.projectid !== -1) vm.project.id = response.projectid;
                testProgMsg(response.running, response.message);
            });
        }

        function stop() {
            $timeout.cancel(timeOuts.initTest);
            tester.stop(vm.project.id).then(function (response) {
                testProgMsg(response.running, response.message);
                if (!response.running) {
                    statusPromises.clear();
                }
            });
        }

        /**
         * @param url {string} url to test
         * @param projectName {string} name of the project
         * @param projectId {number} id of the project}. This is received from back end after calling vm.initTest
         * @param requestCount {number} number of requests to make. this should probably deprecate.
         */
        function pingCapacityStatus(url, projectId) {
            chartTracker.clearFusionCharts();
            chartTracker.clearGoogleCharts();
            statusPromises.clear();
            //hide the performance graph. we don't need it here.
            vm.performanceScalabilityData = null;
            // keep checking for status updates for new data until the test duration + margin is over.
            var capacityPromise = $interval(function () {
                tester.getCapacityStatus(url, projectId).then(function (response) {
                    if (response.statusResponse.running && (vm.charts = response.charts.length) !== 0) {
                        vm.successTitle = "Capacity"
                        vm.charts = response.charts; //array of three charts: one for requests, another for performance, and another for success rate
                        vm.requestsGraphLabels = vm.charts[0].labels;
                        vm.requestsGraphData = [vm.charts[0].data];
                        vm.requestsGraphOptions = chartOptions.requestGraphOptions("Request input: " + url);

                        vm.successScalabilityLabels = vm.charts[1].labels;
                        vm.successScalabilityData = [vm.charts[1].data];
                        vm.successScalabilityOptions = chartOptions.successScalabilityOptions("# of successful requests: " + url);
                        
                        vm.loadTestMessage = "Current known capacity: " + Math.max.apply(null, vm.successScalabilityData[0]) + " requests.";
                    } else {
                        $timeout.cancel(timeOuts.initTest);
                        testProgMsg(response.statusResponse.running, response.statusResponse.message);
                        statusPromises.clear();
                    }
                });
            }, 2000); //check every 2 second.
            statusPromises.add(capacityPromise);
        }

        /**
         * @param url {string} url to test
         * @param projectName {string} name of the project
         * @param projectId {number} id of the project}. This is received from back end after calling vm.initTest
         * @param requestCount {number} number of requests to make. this should probably deprecate.
         */
        function pingScalabilityStatus(url, projectId) {
            chartTracker.clearFusionCharts();
            chartTracker.clearGoogleCharts();
            statusPromises.clear();
            // keep checking for status updates for new data until the test duration + margin is over.
            var scalabilityPromise = $interval(function () {
                tester.getScalabilityStatus(url, projectId).then(function (response) {
                    if (response.statusResponse.running && (vm.charts = response.charts.length) !== 0) {
                        vm.successTitle = "Success Scalability";
                        vm.charts = response.charts; //array of three charts: one for requests, another for performance, and another for success rate
                        vm.requestsGraphLabels = vm.charts[0].labels;
                        vm.requestsGraphData = [vm.charts[0].data];
                        vm.requestsGraphOptions = chartOptions.requestGraphOptions("Request input: " + url);

                        // console.log(JSON.stringify(response));
                        vm.performanceScalabilityLabels = vm.charts[1].labels;
                        vm.performanceScalabilityData = [vm.charts[1].data];
                        vm.performanceScalabilityOptions = chartOptions.performanceScalabilityOptions("Performance Scalability: " + url);

                        vm.successScalabilityLabels = vm.charts[2].labels;
                        vm.successScalabilityData = [vm.charts[2].data];
                        vm.successScalabilityOptions = chartOptions.successScalabilityOptions("Success Scalability: " + url);
                    } else {
                        $timeout.cancel(timeOuts.initTest);
                        testProgMsg(response.statusResponse.running, response.statusResponse.message);
                        statusPromises.clear();
                    }
                });
            }, 2000); //check every 2000 ms, or 2 seconds
            statusPromises.add(scalabilityPromise);
        }

        function scrollToCharts() {
            $anchorScroll('chartsDiv');
        }

        /**
         * For a particular Webservice, get the methods.
         */
        function getMethods(serviceName) {
            if (serviceName) {
                vm.methods = webServices.getMethods(vm.services, serviceName);
                urlBuilder.setBaseUrl(vm.methods[0].baseUri);
                vm.baseUrl = urlBuilder.getBaseUrl();
                vm.project.uri = vm.baseUrl;
            }
        };


        /**
         * For a particular method, get the parameters.
         * @param method
         */
        function getParams(method) {
            if (method) {
                urlBuilder.setMethod(method);
                vm.params = urlBuilder.formatParams(method.parameters);
                urlBuilder.setBaseMethodUrl(method.method);
                vm.baseMethodUrl = urlBuilder.getBaseMethodUrl();
                vm.project.uri = vm.baseMethodUrl;
            }
        }

        /**
         * Dynamically update the parameters.
         */
        function updateParams() {
            var paramString = '';
            vm.params.forEach(function (param, index) {
                if (index === 0) {
                    paramString += "?" + param.key + "=" + param.value;
                } else {
                    paramString += "&" + param.key + "=" + param.value;
                }
            });
            urlBuilder.setFinalUrl(paramString);
            vm.project.uri = urlBuilder.getFinalUrl().trim();
        };

        function initProjectOptions() {
            var options = {};

            //sets up project defaults depending on the type of t est
            switch (vm.project.type) {
                case "availability":
                case "reliability":
                    options.timeUnits = "Seconds";
                    options.interval = 2;
                    options.timeout = 500;
                    break;
                case "capacity":
                    options.userCount = 1; //1 user
                    options.warmUpTime = 1; //1second warm up time
                    options.testTime = 10; //10 second test time
                    options.failuresPermitted = 0; // 0% permitted, anything else triggers a failure
                    options.stepDuration = 500; //500 ms step duration
                    options.stepCount = 2; //exponential factor
                    break;
                case "scalability":
                    options.userCount = 1000; //1000 users
                    options.warmUpTime = 1; //1second warm up time
                    options.testTime = 10; //10 second test time
                    options.failuresPermitted = 0; // 0% permitted, anything else triggers a failure
                    options.stepDuration = 500; //500 ms step duration
                    options.stepCount = 10;
                    options.distribution = "Normal";
                    break;
            }
            vm.project.options = options;
        }

        function serviceConfigValidation() {
            return vm.project.uri !== undefined;
        }

        function testTypeValidation() {
            var valid = true;
            var message = '';
            $timeout.cancel(timeOuts.testType);
            if (!vm.project) {
                valid = false;
                message = "No project was ever created.";
            } else {
                if (!vm.project.type) {
                    valid = false;
                    message = "You must select a test type.";
                } else {
                    if (vm.project.type !== 'performance') {
                        for (option in vm.project.options) {
                            if (vm.project.options[option] === undefined || vm.project.options[option] < 0 || String(vm.project.options[option]).length === 0 ) {
                                valid = false;
                                message = "All options must be configured for the " + vm.project.type + " test.";
                            }
                        }
                    }
                }
            }
            vm.testTypeValidAction = {};
            vm.testTypeValidAction.class = styler.styleResult(valid);
            vm.testTypeValidAction.message = message;
            timeOuts.testType = $timeout(function() {
                vm.testTypeValidAction.message = undefined;
            }, 3000);
            return valid;
        }

        function testProgMsg(running, message) {
            vm.testStatus = running;
            vm.initTestAction.class = styler.styleResult(running);
            vm.initTestAction.message = message;
            timeOuts.initTest = $timeout(function() {
                vm.initTestAction.message = undefined;
            }, 3000);
        }
    }
})();