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
package se.streamsource.streamflow.web.context.administration.external.integrationpoints;


import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.api.external.IntegrationPointDTO;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoint;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoints;

import java.io.IOException;

import static se.streamsource.dci.api.RoleMap.*;

@Mixins( IntegrationPointAdministrationContext.Mixin.class )
public interface IntegrationPointAdministrationContext
   extends Context, IndexContext<IntegrationPointDTO>, DeleteContext
{

   void changedescription( @MaxLength(50) @Name("name") String name )
         throws IllegalArgumentException;

   abstract class Mixin
      implements IntegrationPointAdministrationContext
   {
      @Structure
      Module module;

      public void changedescription( final String name ) throws IllegalArgumentException
      {
         // check if the new description is valid
         IntegrationPoints.Data integrationPoints = role( IntegrationPoints.Data.class );

         if( Iterables.count( Iterables.filter( new Specification<IntegrationPoint>()
         {
            public boolean satisfiedBy( IntegrationPoint item )
            {
               return item.getDescription().equalsIgnoreCase( name );
            }
         }, integrationPoints.integrationPoints() ) ) > 0 )
         {
            throw new IllegalArgumentException( "integrationpoint_already_exists" );
         }

         role( IntegrationPoint.class ).changeDescription( name );
      }

      public IntegrationPointDTO index()
      {
         ValueBuilder<IntegrationPointDTO> builder = module.valueBuilderFactory().newValueBuilder( IntegrationPointDTO.class );

         builder.prototype().systemName().set( role( IntegrationPoint.class).getDescription() );

         return builder.newInstance();
      }

      public void delete() throws IOException
      {
         role(IntegrationPoints.class).removeIntegrationPoint( role(IntegrationPoint.class ) );
      }
   }
}
