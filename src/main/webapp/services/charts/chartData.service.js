(function() {
    angular
        .module('app')
        .factory('chartData', chartData);

    chartData.$inject = ['$http'];

    function chartData($http) {
        var service = {
            getChartData: getChartData,
            getCompareData: getCompareData,
            getStatsData: getStatsData
        }

        return service;

        /**
         * Get data relating to loading a single load (used during a load test or pressing "Load"
         * for a single project
         * @param url
         * @param projectId
         * @param dataSource
         * @returns {*}
         */
        function getChartData(url, projectId, dataSource) {
            var encodedUrl = encodeURIComponent(url);
            return $http.get("v1/test/" + dataSource + "?uri=" + encodedUrl + "&projectId=" + projectId).then(function (response) {
                return response.data;
            });
        }

        /**
         * Get data for making comparisons
         * @param projectList
         * @returns {*}
         */
        function getCompareData(projectList) {
            var encodedUrl = "v1/test/compare?projectlist=" + encodeURIComponent(projectList);
            return $http.get(encodedUrl).then(function (response) {
                return response.data;
            });
        }

        /**
         * Get chart data for  max, mins, avgs, ect.
         * @param projectList
         * @param type
         * @returns {*}
         */
        function getStatsData(projectList, type) {
            var encodedUrl = "v1/test/stats?type=" + encodeURIComponent(type) + "&projectlist=" + encodeURIComponent(projectList);
            return $http.get(encodedUrl).then(function (response) {
                return response.data;
            });
        }
    }
})();