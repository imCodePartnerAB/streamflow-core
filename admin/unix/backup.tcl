#
#
# Copyright 2009-2014 Jayway Products AB
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Create backup
# An export of the current application database will be exported
# to /backup. After running this, copy that file to your backup
# system and then remove it.

source "connect.tcl"
puts [jmx_invoke -m Qi4j:application=StreamflowServer,name=Manager backup]
exit
