(function() {
    angular
        .module('app')
        .factory('statusPromises', statusPromises);

    statusPromises.$inject = ['$interval', '$timeout', 'styler'];

    function statusPromises($interval, $timeout, styler) {
        var promises = [];
        var actionTimeOut;

        var service = {
            clear: clear,
            add: add,
            setUpTO: setUpTO
        }

        return service;

        function clear() {
            promises.forEach(function (promise) {
                $interval.cancel(promise);
            });
        }

        function add(promise) {
            promises.push(promise);
        }

        function setUpTO(message, success, time, vm) {
            $timeout.cancel(actionTimeOut);
            vm.action = message;
            vm.actionClass = styler.styleResult(success);
            actionTimeOut = $timeout(function() {
                vm.action = undefined;
            }, time);
        }
    }
})();