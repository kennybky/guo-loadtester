
(function() {
    angular
        .module('app')
        .controller('WebProjectController', WebProjectController);

    WebProjectController.$inject = [
        '$q',
        'webtester',
        'webProject'
    ];

    function WebProjectController($q, webtester, webProject) {
        var vm = this;
        vm.webtester = webtester;
        vm.createNewProject = createNewProject;
        vm.uploadTest = uploadTest
        vm.webProject = webProject;
        vm.projects = [];
        vm.current_project = null;
        vm.start = start;
        vm.getProjects = getProjects;
        vm.getProject = getProject;
        vm.editProject = editProject;
        vm.currentResponseBody = "";
        vm.saveProject = saveProject;
        vm.paramName = null
        vm.paramValue = null
        vm.addParam = addParam



        getProjects()


        function start(){
            console.log('Running test');
        webtester.start(vm.current_project.title, vm.current_project.url, vm.current_project.method, vm.current_project.parameters)
            .then(function (res) {
                let response = res.data;
                console.log(response.avgResponseTime);
                let stat = new Object()
                stat.url = vm.current_project.url;
                stat.type = vm.current_project.method;
                stat.responseTime = response.avgResponseTime;
                vm.current_project.stats.push(stat)
                vm.currentResponseBody = response.message;
                vm.saveProject()
            },
            (err)=>{
                console.log(err)
            })
    }

    function saveProject(){
        console.log('Saving Project');
            let project = vm.current_project;
            webtester.save(project)
                .then((res)=>{
                    console.log("project saved")
                        console.log(res)
                },
                    (err)=>{
                    consle.log(err)
                    })
        }

        function getProjects() {
            webtester.getProjectsDb()
            .then(
                (res) => {
                    res.data.projects.forEach((p) =>{

                        let project = JSON.parse(p.data);
                        project.dbId = p.id;
                        vm.projects.push(project)
                    })
                    console.log(vm.projects)

                },
                () => {
                    vm.projects = [];
                    console.log('failed to load projects, defaulting to empty :', []);
                });
    }

    function editProject(id){
        vm.current_project = vm.getProject(id);
        console.log(vm.current_project)
        if(vm.current_project.service === 'upload') {
            $('[data-target="#uploadtab"]').tab('show');
        } else {
            $('[data-target="#tab1"]').tab('show');
        }

    }

    function uploadTest(){

    }

    function addParam(){
            if (vm.paramName !=null && vm.paramValue !=null) {
                console.log(`adding {${vm.paramName}, ${vm.paramValue}}`)
                vm.current_project.parameters[vm.paramName] = vm.paramValue;
            }
        }

        function createNewProject(service=null) {
            const project = webProject.newProject(generateTempId());
            project.service = service;

            webtester.createProjectDb(project)
                .then(
                    (res) => {
                console.log('Created a new project with ID ' + res.data.project.id);
                project.dbId = res.data.project.id;
                console.log(project);

                vm.projects.push(project);
            },
            (err) => console.log(err),
                () => console.log('Project Creation Finished'));

            return project;
        }

        function testProject(id) {
                const project = getProject(id);

        }


        function generateTempId() {
            return Math.trunc(Math.random() * 999999999) + 1;
        }

        function createProjectDb(project)  {
            const url = `${vm.baseUrl}/add`;
        return this.http
            .post(url, {id: project.dbId, data: JSON.stringify(project)})
            .map((res) => res.json());
    }

        function getProject(id){
            return vm.projects
                .find((project) => project.id === id);
    }

    }
})();