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

package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationParticipationsQueries;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static se.streamsource.dci.api.RoleMap.role;

/**
 * JAVADOC
 */
public class OrganizationUsersContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      OrganizationParticipationsQueries participants = role( OrganizationParticipationsQueries.class );

      QueryBuilder<User> builder = participants.users();
      Query<User> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

      return new LinksBuilder( module.valueBuilderFactory() ).rel( "user" ).addDescribables( query ).newLinks();
   }

   public LinksValue possibleusers()
   {
      OrganizationParticipationsQueries participants = role( OrganizationParticipationsQueries.class );

      Query<User> query = participants.possibleUsers();

      return new LinksBuilder( module.valueBuilderFactory() ).command( "join" ).addDescribables( query ).newLinks();
   }

   public void join( EntityValue userDTO )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      Organization org = role( Organization.class );

      OrganizationParticipations user = uow.get( OrganizationParticipations.class, userDTO.entity().get() );
      user.join( org );
   }
}
