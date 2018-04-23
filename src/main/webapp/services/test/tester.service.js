(function () {
    angular
        .module('app')
        .factory('tester', tester);

    tester.$inject = ['$http'];

    function tester($http) {
        var service = {
            start: start,
            stop: stop,
            getPerformanceStatus: getPerformanceStatus,
            getScalabilityStatus: getScalabilityStatus,
            getCapacityStatus: getCapacityStatus,
            validateProjectName: validateProjectName
        }

        return service;

        /**
         * Starts a performance, capacity, or scalability test depending on the type.
         * @param project {object} whole project object
         * @projectName {string} name of the project
         * @url {string} url of the service to test
         * @type {string} capacity or scalability.
         * @options {object} json object representing key:value pairs needed for
         *                 either type of test
         * @returns {*} This method returns a promise. Fulfilled when http request is received.
         */
        function start(project) {
            let projectName = project.name, url = project.uri, type = project.type, options = project.options;
            var encodedUrl = encodeURIComponent(url);

            if (type === 'performance') {
                var uri = "v1/test/startPerformanceTest?uri=" + encodedUrl
                    + "&projectName=" + projectName + "&method=" + project.method;
                return $http.get(uri).then(function (response) {
                    return response.data; //response.data is the json returned, response.status is the statuscode
                });
            }

            if (type === 'capacity') {
                console.log('calling capacity url');
                var uri = "v1/test/startCapacityTest?uri=" + encodedUrl + "&projectname=" + projectName
                    + "&userCount=" + options.userCount + "&warmUpTime="
                    + options.warmUpTime + "&testTime=" + options.testTime + "&stepDuration=" + options.stepDuration
                    + "&stepCount=" + options.stepCount + "&failuresPermitted="
                    + options.failuresPermitted+ "&method=" + project.method;
                return $http.get(uri).then(function (response) {
                    return response.data;
                });
            }

            if (type === 'scalability') {
                var uri = "v1/test/startScalabilityTest?uri=" + encodedUrl + "&projectname=" + projectName
                    + "&distribution=" + options.distribution + "&userCount=" + options.userCount + "&warmUpTime="
                    + options.warmUpTime + "&testTime=" + options.testTime + "&stepDuration=" + options.stepDuration
                    + "&stepCount=" + options.stepCount + "&failuresPermitted="
                    + options.failuresPermitted+ "&method=" + project.method;
                return $http.get(uri).then(function (response) {
                    return response.data;
                });
            }

            if (type === 'availability' || type === 'reliability') {
                var interval = intervalToMs(options.interval, options.timeUnits);
                console.log("Ms interval: " + interval);
                return $http.get('v1/test/startScheduledTest?projectName=' + projectName + '&uri=' + encodedUrl
                    + '&interval=' + interval + '&timeout=' + options.timeout+ "&method=" + project.method).then(function (response) {
                    return response.data;
                });
            }
        }

        function intervalToMs(interval, timeUnits) {
            var conversion = 1 / (1000 * 1.0); //by default, assume interval is given in seconds.
            switch(timeUnits) {
                case "Minutes":
                    conversion = 1 / (60000 * 1.0);
                    break;
                case "Hours":
                    conversion = 1 / (3600000 * 1.0);
                    break;
                default:
                    console.log("Interpreting time value as seconds");
                    break;
            }
            return Math.round(interval / conversion);
        }

        function stop(projectId) {
            console.log("stop called");
            return $http.get('v1/test/stop?projectId=' + projectId).then(function (response) {
                return response.data;
            });
        }

        function getPerformanceStatus(projectId) {
            var uri = "v1/test/realTimePerformance?projectId=" + projectId;
            return $http.get(uri).then(function (response) {
                return response.data;
            });
        }

        /**
         * Get's the current running scalability status of a test
         * @param url
         * @param projectName
         * @param projectId
         * @param requestCount
         * @returns {*}
         */
        function getScalabilityStatus(url, projectId) {
            var uri = "v1/test/scalabilityStatus?uri=" + url + "&projectId=" + projectId;
            return $http.get(uri).then(function (response) {
                return response.data;
            });
        }

        /**
         * Get's the current running capacity status of a test
         * @param url
         * @param projectName
         * @param projectId
         * @param requestCount
         * @returns {*}
         */
        function getCapacityStatus(url, projectId) {
            console.log('calling capacity status');
            var uri = "v1/test/capacityStatus?uri=" + url + "&projectId=" + projectId;
            return $http.get(uri).then(function (response) {
                return response.data;
            });
        }

        function validateProjectName(name) {
            var uri = "v1/test/validateProjectName?name=" + name;
            return $http.get(uri).then(function (response) {
                return response.data;
            });
        }
    }
})();