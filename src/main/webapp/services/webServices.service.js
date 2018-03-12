(function() {
    angular
       .module('app')
       .factory('webServices', webService);

    webService.$inject = ['$http'];

    function webService($http) {
        var service = {
            getServices: getServices,
            saveTestUrl: saveTestUrl,
            getSavedUrls: getSavedUrls,
            saveWSDL: saveWSDL,
            serviceDisplays: serviceDisplays,
            getMethods: getMethods,
            deleteService: deleteService,
            formatParams: formatParams
        };

        return service;

        function getServices() {
            return $http.get("v1/service/get").then(function (response) {
                return splitServices(response.data);
            });
        }

        function saveTestUrl(uri, serviceId) {
            var formattedUri;
            if (uri) {
                formattedUri = encodeURIComponent(uri);
            } else {
                formattedUri = "";
            }
            if (serviceId) {
                //save the uri to a service that has attributes used for building the url
                return $http.get("v1/service/saveUrlForBuilder?id=" + serviceId + "&uri=" + formattedUri).then(function (response) {
                    return response;
                });
            } else {
                //save the uri to the service table, but it has no attributes for building the url
                return $http.get("v1/service/saveUrl?uri=" + formattedUri).then(function (response) {
                    return response;
                });
            }
        }

        function getSavedUrls() {
            return $http.get("v1/service/getSavedUrls").then(function (response) {
                return response.data;
            });
        }

        function saveWSDL(name, descriptionUri) {
            if (name == undefined) name = '';
            if (descriptionUri == undefined) {
                descriptionUri = '';
            } else {
                descriptionUri = encodeURIComponent(descriptionUri);
            }
            return $http.get('v1/service/save?name=' + name +
                '&descriptionUri=' + descriptionUri).then(function(response) {
                return response;
            });
        }

        function serviceDisplays(services) {
            var names = [];
            services.forEach(function (service) {
                if (names.indexOf(service.name) === -1) {
                    names.push(service.name);
                }
            });
            return names;
        }

        function getMethods (services, name) {
            var methods = [];
            services.forEach(function (service) {
                if (service.name === name) {
                    methods.push(service);
                }
            });
            return methods;
        }

        function deleteService(service) {
            return $http.get('v1/service/delete?id=' + service.id).then(function(response) {
                return response;
            });
        }

        function formatParams(services) {
            return services.map(function (service) {
                service.parameters = service.parameters.join(",");
                return service;
            });
        }

        function splitServices(services) {
            var servicesWithBuilder = [];
            var servicesWithoutBuilder = []; ///these are services that aren't configured with a url builder, i.e. only testUri is populated

            services.forEach(function(service) {
                if (service.baseUri !== null && service.baseUri !== undefined) {
                    servicesWithBuilder.push(service);
                } else {
                    servicesWithoutBuilder.push(service);
                }
            });

            return {
                servicesWithBuilder: servicesWithBuilder,
                servicesWithoutBuilder: servicesWithoutBuilder
            }
        }
    }
})();