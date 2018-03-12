(function() {
    angular
        .module('app')
        .directive('actionMessage', actionMessage);

    function actionMessage() {
        return {
            restrict: 'E',
            scope: {
                action: '=actionInfo'
            },
            templateUrl: 'templates/action-message.html'
        }
    }
})();