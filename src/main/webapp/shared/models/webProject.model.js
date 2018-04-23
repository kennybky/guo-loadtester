(function() {
    angular
        .module('app')
        .factory('webProject', webProject);

    function webProject() {
        var model = {
            projectTitle: projectTitle,
            newProject: newProject
        };



        function projectTitle(project) {
            return project.title || `PROJECT_${project.id}`
        }

        function newProject(id) {
            return Project.create(id);
        }


        class Project {


            static create(id) {
                return new Project(id);
            }

            static projectTitle(project){
                return project.title || `PROJECT_${project.id}`
            }

            constructor(id) {
                this.id = id;
                this.title = `PROJECT_${id}`;
                this.stats = [];
                this.dbId = null;
                this.description = null;
                this.url = null;
                this.method = "GET";
                this.type = "performance";
                this.name = `PROJECT_${id}`;
                this.options = null;
                this.method = null;
                this.service = null
                this.parameters = {};
                this.parameterEntries = [];
                this.headNode = null;
                this.nodes = [];
                this.edges = [];
            }
        }

        return model;
    }

})();
