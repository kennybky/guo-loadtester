(function() {
    angular
        .module('app')
        .controller('WebServiceController', WebServiceController);

    WebServiceController.$inject = ['$scope', '$location', 'webServices', 'retester', 'tableItems', '$timeout', 'styler', '$q'];

    function WebServiceController($scope, $location, webServices, retester, tableItems, $timeout, styler, $q) {
        var vm = this;
        var timeouts = {};
        vm.serviceBuilders = [];
        vm.serviceNoBuilders = [];
        vm.saveServiceBuilder = saveServiceBuilder;
        vm.saveServiceUrl = saveServiceUrl;
        vm.deleteService = deleteService;
        vm.test = test;
        vm.serviceBuilderAction = {};
        vm.serviceNoBuilderAction = {};
        activate();

        $scope.$on('$destroy', function () {
            for (timeo in timeouts) $timeout.cancel(timeo);
        });

        function activate() {
            webServices.getServices().then(function (services) {
                vm.serviceBuilders = services.servicesWithBuilder;
                vm.serviceNoBuilders = services.servicesWithoutBuilder;
                vm.dataFetched = true;
            });
        }

        function saveServiceBuilder () {
            $timeout.cancel(timeouts.serviceBuilderAction);
            webServices.saveWSDL(vm.name, vm.uri).then(function (success) {
                serviceActionProg(vm.serviceBuilderAction, "serviceBuilderAction", true, success.data);
                activate();
            }, function(error) {
                serviceActionProg(vm.serviceBuilderAction, "serviceBuilderAction", false, error.data);
            });
        }

        function saveServiceUrl() {
            webServices.saveTestUrl(vm.uri).then(function(success) {
                serviceActionProg(vm.serviceNoBuilderAction, "serviceNoBuilderAction", true, success.data);
                activate();
            }, function(error) {
                serviceActionProg(vm.serviceNoBuilderAction, "serviceNoBuilderAction", false, error.data);
            });
        }

        function deleteService(service, builder) {
            if (builder) $timeout.cancel(timeouts.serviceBuilderAction);
            else $timeout.cancel(timeouts.serviceNoBuilderAction);
            webServices.deleteService(service).then(function (success) {
                if (builder) serviceActionProg(vm.serviceBuilderAction, "serviceBuilderAction", true, success.data);
                else serviceActionProg(vm.serviceNoBuilderAction, "serviceNoBuilderAction", true, success.data);
                activate();
            }, function(error) {
                if (builder) serviceActionProg(vm.serviceBuilderAction, "serviceBuilderAction", false, error.data);
                else serviceActionProg(vm.serviceNoBuilderAction, "serviceNoBuilderAction", false, error.data);
            });
        }

        function serviceActionProg(action, timeo, success, message) {
            action.class = styler.styleResult(success);
            action.message = message;
            timeouts[timeo] = $timeout(function() {
                action.message = undefined;
            }, 3000);
        }

        function test (service) {
            retester.setService(service);
            $location.path('/new-project');
        }
    }
})();
