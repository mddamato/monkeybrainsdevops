var angularBoot = angular.module('angularBoot', ['ngRoute', 'angularBootControllers']);

angularBoot.config(['$routeProvider', function ($routeProvider) {

    $routeProvider


        .when('/retrieveString', {
            templateUrl: 'templates/retrieveString.html',
            controller: 'RetrieveStringController'
        })

        .when('/aboutPage', {
            templateUrl: 'templates/about.html',
            controller: 'AboutController'
        })

        .otherwise({
            templateUrl: 'templates/indexTemplate.html',
            redirectTo: '/index',
            controller: 'IndexController'
        });

}]);

// angularBoot.controller('SearchRecord', function ($scope, $http, $location) {
//     $scope.search = function(user) {
//         if (user!=null && user.name != "")
//         {
//             $http.get("endpoints/query/"+user.name).success(function (data) {
//                 $scope.queryObject = data
//                 if($scope.queryObject.biometrics == "" &&
//                     $scope.queryObject.borders == "")
//                     {$scope.msg = "No Data Found!"}
//                 else {$scope.msg = "";}
//             });
//         }
//     };
//
//     $scope.reset = function() {
//         $scope.queryObject = "";
//         $scope.msg = "";
//         if(typeof $scope.user !== "undefined"){$scope.user.name = ""}
//     };
// });
//

 angularBoot.controller('RetrieveStringController', function ($scope, $http, $location) {
	$scope.retrieveString = function() {
	    $scope.queryObject = ""
	    $http.get("/message").success(function (data) {
             $scope.queryObject = data
        }).error(function (data){
            console.log("ERROR:")
            console.log(data)
            $scope.queryObject= "no data"
        });
	};
 });


angularBoot.controller('IndexController', function ($scope, $http, $location) {

});

angularBoot.controller('AboutController', function ($scope, $http, $location) {


});
