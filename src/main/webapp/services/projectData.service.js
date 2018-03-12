(function() {
    angular
        .module('app')
        .factory('projectData', projectData);

    projectData.$inject = ['$http', 'tableItems'];

    function projectData($http, tableItems) {
        var service = {
            getProjects: getProjects,
            deleteProjects: deleteProjects,
            updateLoadBtn: updateLoadBtn,
            getAvailability: getAvailability,
            getReliability: getReliability,
            filterNIP: filterNIP,
            msToSec: msToSec
        };

        return service;

        function getProjects() {
            return $http.get("v1/project/list").then(function (response) {
                var projects = response.data;
                projects[0] = projects[0].map(tableItems.addSelectedAttr); //projects[0] is a list of load test projects
                projects[1] = projects[1].map(tableItems.addSelectedAttr); // projects[1] is a list of scheduled projects (for rel and avail)
                return projects;
            });
        }

        function deleteProjects(projects) {
            var projectIds = projects.map(function (project) {
                return project.projectid;
            });

            return $http.post('v1/project/deleteProjects', {projectIds: projectIds},
                {headers: {'Content-Type': 'application/json'}});
        }

        function updateLoadBtn(projects) {
            var selected = projects.filter(function (project) {
                return project.selected;
            });
            var action = "";
            if (selected.length > 1) {
                action = "Compare";
            }
            else if (selected.length === 1) {
                action = "Load";
            }
            return action;
        }

        function getAvailability(mode, date, projectId) {
            return $http.get('v1/test/availability?date='
                + date.toISOString() + '&mode=' + mode + '&projectId=' + projectId).then(function (response) {
                return response.data;
            });
        }

        function getReliability(projectId) {
            return $http.get('v1/test/reliability?projectId=' + projectId).then(function (response) {
                return response.data;
            });
        }

        function filterNIP(projects) {
            return projects.filter(function(project) {
               return !project.inProgress;
            });
        }

        function msToSec(interval) {
            return interval / 1000;
        }
    }
})();