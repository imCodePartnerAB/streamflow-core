/**
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
package se.streamsource.streamflow.server.plugin.ldapimport;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;

import java.util.List;

/**
 * Contains a list of user detail values.
 */
public interface UserListValue
   extends ValueComposite
{
   Property<List<UserDetailsValue>> users();
}
