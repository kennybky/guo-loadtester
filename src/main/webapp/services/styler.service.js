(function() {
    angular
        .module('app')
        .factory('styler', styler);

    function styler() {
        var service = {
            styleResult: styleResult
        }

        return service;

        function styleResult(success) {
            var className = "actionMsg ";
            if (success) className += "text-success";
            else className += "text-danger";
            return className;
        }
    }
})();