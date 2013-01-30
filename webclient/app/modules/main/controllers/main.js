/*
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function() {
  'use strict';

  var main = angular.module('sf.main.controllers', ['sf.backend.services.project', 'sf.backend.services.case']);

  main.controller('ProjectListCtrl', ['$scope', 'projectService', function($scope, projectService) {
    $scope.projects = projectService.getAll();
  }]);

  main.controller('CaseListCtrl', ['$scope', 'projectService', function($scope, projectService){
    $scope.cases = projectService.getSelected();
  }]);

  main.controller('CaseDetailCtrl', ['$scope', 'caseService', function($scope, caseService){
    $scope.case = caseService.getSelected();
    $scope.contacts = caseService.getSelectedContacts();
  }]);


})();