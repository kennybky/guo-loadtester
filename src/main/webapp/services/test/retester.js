(function() {
    angular
        .module('app')
        .factory('retester', retester);

    function retester() {
        var webService;
        var project;

        var service = {
            setProject: setProject,
            getProject: getProject,
            setService: setService,
            getService: getService
        }

        return service;

        function setProject(retest) {
            project = retest;
            console.log(project)
        }

        function getProject() {
            return project;
        }

        function setService(testService) {
            webService = testService;
        }

        function getService() {
            return webService;
        }
    }
})();