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
.directive('sfRadioExtendedAutoSend', ['$parse', '$routeParams', 'caseService', function($parse, $params, caseService) {
    return {
      require: 'ngModel',
      scope: true,
      link: function(scope, element, attr, ngModel) {

        var hasRunAtLeastOnce = false;
        scope.$watch(attr.ngModel, function (newValue, oldValue, srcScope) {

          if (hasRunAtLeastOnce) {
            console.log("new: ", newValue, ", old: ", oldValue);

            if (newValue == attr.value) {

              var isOther = $parse(attr['sfRadioExtendedAutoSend'])();
              var value;

              if (isOther) {
                value = $("div input[type=text]", $(element).parent().parent()).val();
                $(element).val(value);
              }
              else {
                value = newValue;
              }

              caseService.updateField($params.caseId, scope.$parent.form[0].draftId, attr.id, value);
            }
          }

          hasRunAtLeastOnce = true;
        });
      }
    }
  }]);
