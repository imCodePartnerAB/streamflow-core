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
package se.streamsource.streamflow.web.context.organizations;

import static org.qi4j.api.util.Iterables.count;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.administration.GroupContext;
import se.streamsource.streamflow.web.context.administration.GroupsContext;
import se.streamsource.streamflow.web.context.administration.OrganizationalUnitsContext;
import se.streamsource.streamflow.web.context.administration.OrganizationsContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

/**
 * JAVADOC
 */
public class GroupsContextTest
   extends ContextTest
{
   // Helper methods
   public static void createGroup(String ouName, String name) throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.newCurrentRoleMap();

      playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
      playRole( OrganizationalUnits.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
      playRole( findDescribable(context(OrganizationalUnitsContext.class).index(), ouName));

      context(GroupsContext.class).create(name);

      uow.complete();
   }

   public static void removeGroup( String ouName, String name ) throws IOException, UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.newCurrentRoleMap();

      playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
      playRole( OrganizationalUnits.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
      playRole( findDescribable(context(OrganizationalUnitsContext.class).index(), ouName));

      playRole(findDescribable(context( GroupsContext.class).index(), name));

      context( GroupContext.class).delete();

      uow.complete();
   }

   @BeforeClass
   public static void before() throws UnitOfWorkCompletionException
   {
      OrganizationalUnitsContextTest.createOU( "OU1" );
      clearEvents();
   }

   @AfterClass
   public static void after() throws IOException, UnitOfWorkCompletionException
   {
      OrganizationalUnitsContextTest.removeOU( "OU1" );
      clearEvents();
   }

   @Test
   public void testGroups() throws UnitOfWorkCompletionException, IOException
   {
      // Create group
      {
         createGroup( "OU1", "Group1" );
         eventsOccurred( "createdGroup", "changedDescription", "addedGroup", "changedOwner" );
      }

      // Check that group can be found
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();

         playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
         playRole( OrganizationalUnits.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
         playRole( findDescribable(context( OrganizationalUnitsContext.class).index(), "OU1"));

         Assert.assertThat( count(context(GroupsContext.class).index()), CoreMatchers.equalTo( 1L ));
         uow.discard();
      }

      // Remove group
      {
         removeGroup("OU1", "Group1");
         eventsOccurred( "removedGroup", "changedRemoved" );
      }
   }
}