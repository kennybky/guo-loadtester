(function() {
    angular
        .module('app')
        .factory('wizardProject', wizardProject);
    
    function wizardProject() {
        /**
         *
         * @param name {string} name of the project
         * @param type {string} type of project, i.e. scalability, capacity
         * @param options {object} options of project; for example, for scalability, it would be 'distribution type':
         * 'normal' or for capacity it would be {'users': 100, 'duration': 10'}
         * @param uri {string} uri to test
         * @constructor Makes Project objects that can be instantiated from any controller.
         */
        return function (name, type, options, uri, method) {
            this.name = name;
            this.type = type;
            this.options = options;
            this.uri = uri;
            this.params = [];
            this.method = method;
        }
    }
})();