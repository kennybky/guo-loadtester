
(function() {
    angular
        .module('app')
        .controller('WebProjectController', WebProjectController);

    WebProjectController.$inject = [
        '$scope',
        '$q',
        '$anchorScroll',
        'webtester',
        'webProject',
        'tester',
        'realTimeTracker',
        '$timeout',
        'chartTracker',
        'statusPromises',
        'chartLoader'
    ];

    function WebProjectController( $scope, $q, $anchorScroll,  webtester,
                                   webProject, tester, realTimeTracker, $timeout, chartTracker, statusPromises, chartLoader) {
        var vm = this;
        vm.createNewProject = createNewProject;
        vm.uploadTest = uploadTest;
        vm.projects = [];
        vm.current_project = null;
        vm.start = start;
        vm.getProjects = getProjects;
        vm.getProject = getProject;
        vm.editProject = editProject;
        vm.loadGraph = loadGraph;
        vm.currentResponseBody = "";
        vm.saveProject = saveProject;
        vm.paramName = null
        vm.paramValue = null
        vm.addParam = addParam
        vm.size = 1;
        vm.start = start;
        vm.constructUri = constructUri;
        vm.RTPERF= false;
        vm.pingProm = null;
        vm.stop = stop;
        vm.deleteProject = deleteProject;
        vm.showLoadingBall = false;
        vm.loadedProject = null;
        vm.addUrl = addUrl;



        getProjects()

        $scope.$on('$destroy', function () {
            chartTracker.clearGoogleCharts();
            chartTracker.clearFusionCharts();
            statusPromises.clear();
        });


        function start(){
            console.log('Running test');
            let prom = $q.defer();
            let current = vm.current_project;
            testAll(prom, vm.current_project.parameterEntries, current)

            prom.promise.then((stats)=>{
                current.stats = stats;
                loadGraph(current.id);
                saveProject(current)
            })
    }

    function testAll(promise, urls, project){
           let stats = []
        urls.forEach((data)=>{
            webtester.start(project.title, data.url, data.method, data.parameters)
                .then(function (res) {
                        let response = res.data;
                        console.log(response.avgResponseTime);
                        let stat = {}
                        stat.url = data.url
                        stat.type = data.method;
                        stat.responseTime = response.avgResponseTime;
                        stats.push(stat)
                        vm.saveProject(project)
                    if (stats.length === urls.length){
                            promise.resolve(stats)
                    }
                    },
                    (err)=>{
                        console.log(err)
                        promise.reject(err)
                    })

        })
    }

    function addUrl(){
            let url = vm.current_project.uri;
            let method = vm.current_project.method
            let params = vm.current_project.parameters;
            let obj = {}
            obj.url = url;
            obj.method = method;
            obj.parameters = params
            vm.current_project.parameterEntries.push(obj)
    }

    function saveProject(project = vm.current_project){
        console.log('Saving Project');
            webtester.save(project)
                .then((res)=>{
                    console.log("project saved")
                        console.log(res)
                },
                    (err)=>{
                    console.log(err)
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
           // $('[data-target="#uploadtab"]').tab('show');
        } else {
            $('[data-target="#tab1"]').tab('show');
        }

    }



    function uploadTest(){
        //var initTestProm = $q.defer();
        vm.current_project.url = constructUri(vm.current_project.url);
            //initTestProm.resolve(response);
        vm.current_project.running = true;
        vm.current_project.uploadSize = vm.size;
                realTimeTracker.setProject(vm.current_project);
               vm.pingProm = realTimeTracker.startPinging();
               vm.RTPERF = true;
        $anchorScroll('chart-section');



    }

    function stop(){
        vm.current_project.running = false;
            vm.pingProm.resolve("Project stopped");
        vm.RTPERF = false;

    }

        /*function testProgMsg(running, message) {
            vm.testStatus = running;
            vm.initTestAction.class = styler.styleResult(running);
            vm.initTestAction.message = message;
            timeOuts.initTest = $timeout(function() {
                vm.initTestAction.message = undefined;
            }, 3000);
        }*/

        function startUpload()  {
          /*  let project = {
                uri : "http://localhost:8080/loadtester/v1/upload/test",
                name : vm.current_project.title,
                type: "performance",
                method : "POST",
                options : null,
                params: null
        }

        project.uri = constructUri()
            return tester.start(project)*/
          vm.current_project.url = constructUri(vm.current_project.url);


        }

        function constructUri (uri){
            let i = uri.indexOf("size")
            if( i > -1){
               uri =  uri.substr(0, i)
            }
            let size = parseInt(vm.size);
            size *= 1024;
            return `${uri}?size=${size}`;
        }

    function addParam(){
            if (vm.paramName !=null && vm.paramValue !=null) {
                console.log(`adding {${vm.paramName}, ${vm.paramValue}}`)
                vm.current_project.parameters[vm.paramName] = vm.paramValue;
            }
        }

        function deleteParam(key){

        }

        function deleteProject(id){
            const project = getProject(id);
                webtester.deleteProjectDb(project.dbId).then((res)=>{
                    console.log(res)
                    vm.projects = vm.projects.filter((pj)=>{
                        return pj.id !== id;
                    })
                },(err)=>{
                    console.log(err)
                })
        }

        function createNewProject(service=null) {
            const project = webProject.newProject(generateTempId());
            project.service = service;
            if (service === 'upload'){
                project.url = "http://localhost:8080/loadtester/v1/upload/test";
                project.method = 'POST'
            }

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

        function loadGraph(id) {
                const project = getProject(id);
                vm.loadedProject = project;
                if(project.service === 'upload'){
                    webtester.getGraphData(project.dbId).then((res)=>{
                        let graphData = res.data;
                        console.log(graphData)
                        vm.showLoadingBall = true;
                            statusPromises.clear();
                            chartTracker.clearGoogleCharts();
                            chartTracker.clearFusionCharts();
                            $anchorScroll('chart-section');
                                    chartLoader.renderFusionChart(graphData.fusionChart);
                                    vm.showLoadingBall = false;


                    }, (err)=>{
                        console.log(err)
                    })
                } else {
                    chartLoader.renderBarChart(project);
                }

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