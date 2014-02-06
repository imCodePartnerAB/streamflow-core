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

  // Using customSelect from http://adam.co/lab/jquery/customselect/
  // See http://jsfiddle.net/p3ZMR/3/ for another example of doing this
  sf.directives.directive('customSelect', ['$location', function (location) {
    return {
      restrict:'A',
      require: "ngModel",
      link: function (scope, element, attrs, ngModel) {
        $(element).customSelect({customClass:'custom-select'});

        // update inner div when model changed
        scope.$watch(attrs.ngModel, function() {
          $(element).trigger('update');
        });
      }
    };
  }]);
})();
