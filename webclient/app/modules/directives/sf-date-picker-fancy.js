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
(function () {
  'use strict';

  sf.directives.directive('sfDatePickerFancy', ['fancyDateService', function (fancyDateService) {

    return {
      restrict:'A',
      require: "ngModel",

      link: function (scope, element, attrs, ngModel) {
        var $element = $(element);
        $element.pickadate({
          selectYears: true,
          selectMonths: true,
          format: 'yyyy-mm-dd',
          onSet: function () {
            // Time must be set and be in the future (but will be ignored on retrieval).
            var isoDate = $element.val() + "T23:59:59.000Z";

            // Trigger ng-change, send ISO-date to controller.
            $element.val(isoDate).trigger('input');
          },
          onClose: function () {
            // Set fancy date without triggering ng-change.
            var date = $element.val().split("T")[0];
            $element.val(fancyDateService.format(date)).blur();
          }
       });
      }
    };
  }]);
})();
