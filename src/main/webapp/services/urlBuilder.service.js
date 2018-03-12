(function() {
    angular
        .module('app')
        .factory('urlBuilder', urlBuilder);

    function urlBuilder() {
        var baseUrl;
        var baseMethodUrl;
        var finalUrl;
        var method;

        var service = {
            getBaseUrl: getBaseUrl,
            formatParams: formatParams,
            setBaseUrl: setBaseUrl,
            setMethod: setMethod,
            getMethod: getMethod,
            setBaseMethodUrl: setBaseMethodUrl,
            getBaseMethodUrl: getBaseMethodUrl,
            getFinalUrl: getFinalUrl,
            setFinalUrl: setFinalUrl
        }

        return service;

        function getBaseUrl() {
            return baseUrl;
        }

        function formatParams(parameters) {
            var params = [];
            parameters.forEach(function (param) {
                if (param && param !== '') {
                    params.push({
                        key: param,
                        value: ''
                    })
                }
            });
            return params;
        }

        function setBaseUrl(url) {
            baseUrl = url;
        }

        function setMethod(service) {
            method = service;
        }

        function getMethod() {
            return method;
        }

        function setBaseMethodUrl(method) {
            baseMethodUrl = baseUrl + method;
        }

        function getBaseMethodUrl() {
            return baseMethodUrl;
        }

        function getFinalUrl() {
            return finalUrl;
        }

        function setFinalUrl(paramStr) {
            finalUrl = baseMethodUrl + paramStr;
        }
    }
})();