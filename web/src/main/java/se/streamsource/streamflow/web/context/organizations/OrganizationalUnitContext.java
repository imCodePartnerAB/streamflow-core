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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.streamflow.domain.organization.MergeOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.MoveOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.OpenProjectExistsException;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;

/**
 * JAVADOC
 */
public class OrganizationalUnitContext
      implements DeleteContext
{
   @Structure
   UnitOfWorkFactory uowf;

   public void move( EntityValue moveValue ) throws ResourceException
   {
      OrganizationalUnitRefactoring ou = RoleMap.role( OrganizationalUnitRefactoring.class );
      OrganizationalUnits toEntity = uowf.currentUnitOfWork().get( OrganizationalUnits.class, moveValue.entity().get() );

      try
      {
         ou.moveOrganizationalUnit( toEntity );
      } catch (MoveOrganizationalUnitException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
      }
   }

   public void merge( EntityValue moveValue ) throws ResourceException
   {
      OrganizationalUnitRefactoring ou = RoleMap.role( OrganizationalUnitRefactoring.class );
      OrganizationalUnit toEntity = uowf.currentUnitOfWork().get( OrganizationalUnit.class, moveValue.entity().get() );

      try
      {
         ou.mergeOrganizationalUnit( toEntity );
      } catch (MergeOrganizationalUnitException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
      }
   }

   public void delete() throws ResourceException
   {
      OrganizationalUnitRefactoring ou = RoleMap.role( OrganizationalUnitRefactoring.class );

      try
      {
         ou.deleteOrganizationalUnit();

      } catch (OpenProjectExistsException pe)
      {
         throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, pe.getMessage() );
      }
   }
}
