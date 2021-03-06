/*
 *
 * Copyright 2009-2014 Jayway Products AB
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
'use strict';

angular.module('sf')
  .controller('CaseListCtrl', function(growl, $scope, $routeParams, projectService, $rootScope) {
    $scope.currentCases = projectService.getSelected($routeParams.projectId, $routeParams.projectType);
    $scope.projectType = $routeParams.projectType;

    $scope.showSpinner = {
      currentCases: true
    };

    //Set breadcrumbs to case-owner if possible else to project id
    $scope.currentCases.promise.then(function(response){
     var owner = _.filter(response, function(sfCase){
        if(sfCase.owner.length > 0){
          return sfCase.owner;
        }
      });

      if(owner.length > 0){
        $rootScope.$broadcast('breadcrumb-updated', [{owner: response[0].owner}, {projectType: $routeParams.projectType}]);
      } else {
        $rootScope.$broadcast('breadcrumb-updated', [{projectId: $routeParams.projectId}, {projectType: $routeParams.projectType}]);
      }

      $scope.showSpinner.currentCases = false;
    });

    var updateObject = function(itemToUpdate){
      itemToUpdate.invalidate();
      itemToUpdate.resolve();
    };
    
    // Event listeners
    $rootScope.$on('case-created', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-closed', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-assigned', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-unassigned', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-resolved', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-deleted', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('case-owner-changed', function(){
      updateObject($scope.currentCases);
    });
    $rootScope.$on('casedescription-changed', function(){
      updateObject($scope.currentCases);
    });
    // End Event listeners
  });