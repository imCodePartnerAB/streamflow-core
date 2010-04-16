/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.SubContext;
import se.streamsource.streamflow.web.context.access.SurfaceContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationsContext;
import se.streamsource.streamflow.web.context.task.TasksContext;
import se.streamsource.streamflow.web.context.users.UsersContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.user.Users;

/**
 * JAVADOC
 */
@Mixins(RootContext.Mixin.class)
public interface RootContext
   extends Context
{
   /**
    * Users context. Here is where you access all users, and methods to create users.
    */
   @SubContext
   UsersContext users();

   /**
    * Here is where you access all tasks, including search
    */
   @SubContext
   TasksContext tasks();

   @SubContext
   OrganizationsContext organizations();

   @SubContext
   SurfaceContext surface();

   abstract class Mixin
      extends ContextMixin
      implements RootContext
   {
      public UsersContext users()
      {
         context.playRoles(module.unitOfWorkFactory().currentUnitOfWork().get( Users.class, UsersEntity.USERS_ID ));
         return subContext( UsersContext.class );
      }

      public TasksContext tasks()
      {
         return subContext( TasksContext.class );
      }

      public OrganizationsContext organizations()
      {
         context.playRoles(module.unitOfWorkFactory().currentUnitOfWork().get( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID ));
         return subContext( OrganizationsContext.class );
      }

      public SurfaceContext surface()
      {
         return subContext( SurfaceContext.class );
      }
   }
}
