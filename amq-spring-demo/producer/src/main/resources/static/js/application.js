var angularBoot = angular.module('angularBoot', ['ngRoute', 'angularBootControllers']);

angularBoot.config(['$routeProvider', function ($routeProvider) {

    $routeProvider


        .when('/sendString', {
            templateUrl: 'templates/sendString.html',
            controller: 'SendStringController'
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

 angularBoot.controller('SendStringController', function ($scope, $http, $location) {

	$scope.sendInputForm = function() {

		var formObject = $scope.inputForm;

		var params = JSON.stringify(formObject);

		console.log(params)

		$http.post("/sendString", params)
			.success(
				function(data, status) {
					console.log(data)
				})
			.error(
				function(data) {
					console.log(data)
				}
			)

		$location.url('/sendString');

	};

 });


angularBoot.controller('IndexController', function ($scope, $http, $location) {

});

angularBoot.controller('AboutController', function ($scope, $http, $location) {


});
