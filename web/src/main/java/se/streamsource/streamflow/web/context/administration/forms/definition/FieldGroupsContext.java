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
package se.streamsource.streamflow.web.context.administration.forms.definition;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroup;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroups;

/**
 * JAVADOC
 */
public class FieldGroupsContext
      implements IndexContext<Iterable<FieldGroup>>, CreateContext<String, FieldGroup>
{
   @Structure
   Module module;

   public Iterable<FieldGroup> index()
   {
      return role( FieldGroups.Data.class ).fieldGroups();
   }

   public FieldGroup create( @Name("name") String name )
   {
      FieldGroups fieldGroups = role( FieldGroups.class );

      return fieldGroups.createFieldGroup( name );
   }
}