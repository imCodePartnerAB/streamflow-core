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

  var sfNotes = angular.module('sf.controllers.notes', ['sf.services.case']);

  sfNotes.controller('NotesHistoryCtrl', ['$scope', 'caseService', '$routeParams',
    function($scope, caseService, $params) {

      $scope.projectId = $params.projectId;
      $scope.projectType = $params.projectType;
      $scope.caseId = $params.caseId;

      $scope.notesHistory = caseService.getAllNotes($params.projectId, $params.projectType, $params.caseId);
    }]);

})();