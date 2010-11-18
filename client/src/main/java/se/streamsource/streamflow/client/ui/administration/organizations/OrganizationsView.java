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

package se.streamsource.streamflow.client.ui.administration.organizations;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;

import javax.swing.Action;
import java.awt.Component;

public class OrganizationsView
      extends ListDetailView
{
   private OrganizationsModel model;

   public OrganizationsView( @Service ApplicationContext context, @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf )
   {
      model = obf.newObjectBuilder( OrganizationsModel.class ).use( client ).newInstance();

      initMaster( new EventListModel<LinkValue>( model.getList()), null, new Action[0], new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            OrganizationUsersView organizationView = obf.newObjectBuilder( OrganizationUsersView.class ).use( client.getClient( detailLink ).getSubClient( "organizationusers" ) ).newInstance();
            return organizationView;
         }
      });

      new RefreshWhenVisible(this, model );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );
   }
}
