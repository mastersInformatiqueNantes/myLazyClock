var controller = angular.module('myLazyClock.controller.signin', []);

controller.controller('myLazyClock.controller.signin', ['$scope', '$localStorage', '$interval', '$state', 'GApi',
    function signinCtl($scope, $localStorage, $interval, $state, GApi) {
        var interval;
        var interval2;

        var checkLink = function(id) {
                GApi.execute('myLazyClock', 'alarmClock.item', {alarmClockId: id}).then( function(resp) {
                        if(resp.user != undefined) {
                            $interval.cancel(interval);
                            $state.go('webapp.home');
                        }
                    }, function(resp) {
                        $interval.cancel(interval);
                        $localStorage.$reset();
                        check();
                    });
            }

    	var goToHome = function(id) {
    		checkLink(id);
    		interval = $interval(function() {
				checkLink(id);
			}, 4000);
    	}

        var generate = function() {
            interval2 = $interval(function() {
                GApi.execute('myLazyClock', 'alarmClock.generate').then( function(resp) {
                    $localStorage.alarmClockId = resp.id;
                    $scope.alarmClockId = resp.id;
                    $interval.cancel(interval2);
                    goToHome($localStorage.alarmClockId);
                });
            }, 1000);
        }

        var check = function() {
            if($localStorage.alarmClockId == undefined) {
                generate();
            } else {
                $scope.alarmClockId = $localStorage.alarmClockId;
                goToHome($localStorage.alarmClockId);
            }
        }
    	
        check();
        
    }
])