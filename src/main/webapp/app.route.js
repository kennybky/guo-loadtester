(function () {
    angular
        .module('app')
        .config(routeConfig);

    function routeConfig($routeProvider) {
        $routeProvider
            .when(
                '/services', {
                    templateUrl: 'web-services/web-services.html',
                    controller: 'WebServiceController',
                    controllerAs: 'vm'
                })
            .when('/new-project', {
                templateUrl: 'new-project/new-project.html',
                controller: 'NewProjectController',
                controllerAs: 'vm'
            })
            .when('/existing-projects', {
                templateUrl: 'existing-project/existing-project.html',
                controller: 'ExistingProjectController',
                controllerAs: 'vm'
            })
            .otherwise({
                redirectTo: '/new-project'
            });
    }
})();