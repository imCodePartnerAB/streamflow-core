/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context.users;

import org.junit.*;
import org.qi4j.api.unitofwork.*;
import se.streamsource.dci.api.*;
import se.streamsource.streamflow.resource.user.*;
import se.streamsource.streamflow.web.context.*;
import se.streamsource.streamflow.web.context.administration.*;
import se.streamsource.streamflow.web.domain.entity.organization.*;
import se.streamsource.streamflow.web.domain.entity.user.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

import java.io.*;

import static org.hamcrest.CoreMatchers.*;

/**
 * JAVADOC
 */
public class UsersContextTest
   extends ContextTest
{
   // Helper methods
   public static void createUser(String name) throws UnitOfWorkCompletionException
   {
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( Users.class, UsersEntity.USERS_ID);
         context( UsersContext.class).createuser( value( NewUserCommand.class, "{'username':'"+name+"','password':'"+name+"'}") );
         uow.complete();
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
         playRole( Organization.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
         context( OrganizationUsersContext.class).join( entityValue("test" ) );
         uow.complete();
      }
   }

   public static void removeUser(String name) throws IOException, UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.newCurrentRoleMap();
      playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
      playRole( Organization.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
      playRole( User.class, "test");

      context( OrganizationUserContext.class).delete();

      uow.complete();
   }

   @Test
   public void testCreateUser() throws UnitOfWorkCompletionException
   {
      // Create user
      clearEvents();
      createUser( "test" );
      eventsOccurred( "createdUser", "joinedOrganization" );

      // Check that user can be found
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( Users.class, UsersEntity.USERS_ID);

         Assert.assertThat( valueContains( context(UsersContext.class).index(), "test" ), equalTo(true ));

         uow.discard();
      }
   }
}
