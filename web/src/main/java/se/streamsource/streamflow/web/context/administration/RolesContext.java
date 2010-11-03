/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.role.Roles;

/**
 * JAVADOC
 */
@Mixins(RolesContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface RolesContext
      extends IndexContext<LinksValue>, Context
{
   void createrole( @MaxLength(50) StringValue name );

   abstract class Mixin
         implements RolesContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         Roles.Data roles = RoleMap.role( Roles.Data.class );

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "role" ).addDescribables( roles.roles() ).newLinks();
      }

      public void createrole( StringValue name )
      {
         Roles roles = RoleMap.role( Roles.class );

         roles.createRole( name.string().get() );
      }
   }
}