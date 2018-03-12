(function() {
    angular.module('app').config(angularMaterialConfig);

    function angularMaterialConfig($mdThemingProvider) {
        $mdThemingProvider
            .theme('default')
            .primaryPalette('light-blue', {
                'default': '600'
            });
    }
})();
