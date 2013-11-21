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

  var sfCase = angular.module('sf.controllers.case', ['sf.services.case', 'sf.services.navigation', 'sf.services.perspective', 'sf.services.project']);

  sfCase.controller('CaseOverviewCtrl', ['$scope', '$routeParams', 'perspectiveService', 'navigationService',
    function($scope, $params, perspectiveService, navigationService) {
      $scope.perspectives = perspectiveService.getPerspectives();
      /*$scope.myCases = perspectiveService.getMyCases();
      $scope.myLatestCases = perspectiveService.getMyLatestCases();
      $scope.myTodaysCases = perspectiveService.getTodaysCases();*/
    }]);

  sfCase.controller('CaseSearchCtrl', ['$scope', '$routeParams', 'caseService', 'navigationService',
    function($scope, $params, caseService, navigationService) {

    }]);

  sfCase.controller('CaseListCtrl', ['$scope', '$routeParams', 'projectService', 'commonService', function($scope, $params, projectService, commonService) {

    $scope.common = commonService.common;
    $scope.common.currentCases = projectService.getSelected($params.projectId, $params.projectType);
    
    $scope.$on('case-created', function() {
      $scope.cases.invalidate();
    });
  }]);

  sfCase.controller('CaseDetailCtrl', ['$scope', '$timeout', '$routeParams', 'growl', 'caseService', 'navigationService', 'commonService', 'projectService', 'profileService',
    function($scope, $timeout, $params, growl, caseService, navigationService, commonService, projectService, profileService){
    $scope.projectId = $params.projectId;
    $scope.projectType = $params.projectType;

    $scope.caze = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
    $scope.notes = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);
    
    $scope.commands = caseService.getSelectedCommands($params.projectId, $params.projectType, $params.caseId);
    $scope.profile = profileService.getCurrent();

    $scope.common = commonService.common;
    $scope.common.currentCases = projectService.getSelected($params.projectId, $params.projectType);

    $scope.$on('case-created', function() {
        $scope.caze.invalidate();
    });

    $scope.$on('case-changed', function() {
      $scope.caze.invalidate();
      $scope.caze.resolve();
    });

    $scope.addWarning = function(){alert("warning")}
    $scope.addSpecialWarnMessage = function(message) {
      growl.addWarnMessage(message);
    }

    // Mark the case as Read after the ammount of time selected in profile.
    // TODO <before uncomment>. Find a way to update possible commands after post.
    /*$scope.$watch("commands[0] + profile[0]", function(){   
      var commands = $scope.commands;
      var profile = $scope.profile[0];

      $scope.canRead = _.any(commands, function(command){
        return command.rel === "read";
      });

      if ($scope.canRead) {
        $timeout(function() { 
          caseService.Read($params.projectId, $params.projectType, $params.caseId);
        }, profile.markReadTimeout * 1000)

      }
    });*/
  }]);

  sfCase.controller('PrintCtrl', ['$scope', '$routeParams', 'caseService', 'navigationService',
    function($scope, $params, caseService, navigationService){
    $scope.projectId = $params.projectId;
    $scope.projectType = $params.projectType;

    $scope.caze = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
    $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);
    $scope.notes = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);

    $scope.$on('case-created', function() {
        $scope.caze.invalidate();
    });

    $scope.$on('case-changed', function() {
      $scope.caze.invalidate();
      $scope.caze.resolve();
    });

    $scope.$watch('caze + general + notes', function() {
      setTimeout(function(){
         window.print();
      }, 500);
    })
  }]);

  sfCase.controller('CaseEditCtrl', ['$scope', '$routeParams', 'caseService', 'navigationService',
    function($scope, $params, caseService, navigationService) {
      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;

      $scope.caze = caseService.getSelected($params.projectId, $params.projectType, $params.caseId);
      $scope.general = caseService.getSelectedGeneral($params.projectId, $params.projectType, $params.caseId);

      $scope.notes = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);
      $scope.cachedNote = caseService.getSelectedNote($params.projectId, $params.projectType, $params.caseId);

      $scope.possibleCaseTypes = caseService.getPossibleCaseTypes($params.projectId, $params.projectType, $params.caseId);

      $scope.addNote = function($event){
        $event.preventDefault();
        if ($scope.notes[0].note !== $scope.cachedNote[0].note)
          caseService.addNote($params.projectId, $params.projectType, $params.caseId, $scope.notes[0]).then(function(){
            var href = navigationService.caseHref($params.caseId);
            $scope.notes.invalidate();
            $scope.notes.resolve();
            window.location.assign(href);
          });
      }

    }]);

})();
