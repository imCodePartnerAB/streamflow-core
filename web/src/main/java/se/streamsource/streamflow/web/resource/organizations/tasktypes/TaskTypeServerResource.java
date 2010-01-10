/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource.organizations.tasktypes;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskTypes;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.security.AccessControlException;

/**
 * Mapped to:
 * /organizations/{organization}/tasktype/{tasktype}
 */
public class TaskTypeServerResource
      extends CommandQueryServerResource
{
   @Structure
   protected UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   public void changedescription( StringDTO stringValue ) throws ResourceException
   {
      String projectId = (String) getRequest().getAttributes().get( "tasktype" );
      Describable describable = uowf.currentUnitOfWork().get( Describable.class, projectId );

      String newName = stringValue.string().get();

      checkPermission( describable );
      describable.changeDescription( newName );
   }

   public void deleteOperation() throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String org = getRequest().getAttributes().get( "organization" ).toString();

      TaskTypes taskTypes = uow.get( TaskTypes.class, org );

      String identity = getRequest().getAttributes().get( "tasktype" ).toString();
      TaskType taskType = uow.get( TaskType.class, identity );

      try
      {
         checkPermission( taskTypes );
         taskTypes.removeTaskType( taskType );
      } catch (AccessControlException ae)
      {
         throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN );
      }
   }
}