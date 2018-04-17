(function () {
    angular
        .module('app')
        .factory('webtester', webtester);

    webtester.$inject = ['$http'];

    function webtester($http) {
        var service = {
            start: start,
            createProjectDb : createProjectDb,
            getProjectsDb: getProjectsDb,
            save: save
        };

        return service;

        /**
         * Starts a performance, capacity, or scalability test depending on the type.
         * @param projectName {string} name of the project
         * @param url {string} url of the service to test
         * @param type {string} capacity or scalability.
         * @param options {object} json object representing key:value pairs needed for
         *                 either type of test
         * @returns {*} This method returns a promise. Fulfilled when http request is received.
         */
        function start(projectName, url, method, params) {
            var encodedUrl = encodeURIComponent(url);
            let obj = new Object();
            obj.method = method;
            obj.params = params;
            obj.uri = url;
            console.log(obj)
            var uri = "v1/ws/query";
            return $http.post(uri, JSON.stringify(obj));
        }

        function save (project) {
            const url = `v1/webprojects/${project.dbId}`
            return $http.put(url,  {id: project.dbId, data: JSON.stringify(project)});
        }

        function createProjectDb(project)  {
            const url = `v1/webprojects/add`;
            return $http
                .post(url, {id: project.dbId, data: JSON.stringify(project)})
        }

       function getProjectsDb() {
            return $http
                .get("v1/webprojects/")
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
    }
})();