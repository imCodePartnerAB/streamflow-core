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

package se.streamsource.streamflow.web.context.access.accesspoints.endusers;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;
import se.streamsource.streamflow.web.domain.structure.user.EndUsers;

/**
 * JAVADOC
 */
@Mixins(EndUsersContext.Mixin.class)
public interface EndUsersContext
   extends SubContexts<EndUserContext>, Interactions, IndexInteraction<LinksValue>
{
   // command
   void createenduser( StringValue name );

   abstract class Mixin
      extends InteractionsMixin
      implements EndUsersContext
   {

      public LinksValue index()
      {
         EndUsers.Data data = context.get( EndUsers.Data.class );

         TitledLinksBuilder linksBuilder = new TitledLinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( data.anonymousEndUsers() );

         return linksBuilder.newLinks();
      }

      public void createenduser( StringValue name )
      {
         EndUsers endUsers = context.get( EndUsers.class );
         endUsers.createAnonymousEndUser( name.string().get() );
      }

      public EndUserContext context( String id)
      {
         AnonymousEndUser endUser = module.unitOfWorkFactory().currentUnitOfWork().get( AnonymousEndUser.class, id );

         context.set( endUser );
         return subContext( EndUserContext.class );
      }
   }
}