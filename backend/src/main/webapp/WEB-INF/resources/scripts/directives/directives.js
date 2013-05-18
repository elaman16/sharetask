/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
'use strict';

/* Directives */
angular.module('shareTaskApp.directives', []).
	directive('appVersion', ['version', function(version) {
		return function(scope, elm, attrs) {
			elm.text(version);
		};
	}])
	.directive('stopEvent', function () {
		return {
			restrict: 'A',
			link: function (scope, element, attr) {
				element.bind(attr.stopEvent, function (e) {
					e.stopPropagation();
				});
			}
		};
	})
	.directive('focus', function() {
		console.log("focus");
	    return function(scope, element, attrs) {
	        scope.$watch(attrs.focus, function(newValue) {
	            newValue && element[0].focus();
	        });
	    };
	})
	.directive('bDatepicker', function() {
		return {
			require: '?ngModel',
			restrict: 'A',
			link: function($scope, element, attrs, controller) {
				var updateModel;
				updateModel = function(ev) {
					element.datepicker('hide');
					element.blur();
					return $scope.$apply(function() {
						console.log("controller: %o", controller);
						console.log("date: %o", ev.date);
						return controller.$setViewValue(ev.date);
					});
				};
				if (controller != null) {
					controller.$render = function() {
						element.datepicker().data().datepicker.date = controller.$viewValue;
						element.datepicker('setValue');
						element.datepicker('update');
						return controller.$viewValue;
					};
				}
				return attrs.$observe('bDatepicker', function(value) {
					var options;
					options = {};
					if (angular.isObject(value)) {
						options = value;
					}
					if (typeof(value) === "string" && value.length > 0) {
						options = angular.fromJson(value);
					}
					return element.datepicker(options).on('changeDate', updateModel);
				});
			}
		};
	})
	;