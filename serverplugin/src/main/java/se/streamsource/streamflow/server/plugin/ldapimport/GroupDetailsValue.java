/**
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
package se.streamsource.streamflow.server.plugin.ldapimport;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.List;

/**
 * Contains details about a group.
 */
public interface GroupDetailsValue
   extends ValueComposite
{
   /**
    *  The name of the group.
    * @return
    */
   Property<String> name();

   /**
    * The unique identifier in ldap i.e. dn
    * @return
    */
   Property<String> id();

   /**
    * The list of unique members for this group
    * @return
    */
   @UseDefaults
   Property<List<GroupMemberDetailValue>> members();
}
