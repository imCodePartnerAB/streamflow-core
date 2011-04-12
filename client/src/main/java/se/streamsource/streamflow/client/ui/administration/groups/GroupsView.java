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

package se.streamsource.streamflow.client.ui.administration.groups;

import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class GroupsView
      extends ListDetailView
{
   @Uses
   private Iterable<NameDialog> nameDialogs;

   @Uses
   private Iterable<ConfirmationDialog> confirmationDialog;

   private GroupsModel model;

   @Service
   private DialogService dialogs;

   public GroupsView( @Service ApplicationContext context, @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf)
   {
      this.model = obf.newObjectBuilder( GroupsModel.class ).use( client ).newInstance();

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("add"), new javax.swing.Action[]{am.get( "rename" ), am.get( "remove" )}, new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            CommandQueryClient participantsClient = client.getClient( detailLink ).getSubClient( "participants" );
            return obf.newObjectBuilder( ParticipantsView.class ).use( participantsClient).newInstance();
         }
      });

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add()
   {
      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_group_title ) );
      final String name = dialog.name();
      if (!Strings.empty( name ))
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.create( name );
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();

      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( selected.text().get() );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.remove( selected );
            }
         };
      } else
         return null;
   }

   @Action
   public Task rename()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();

      final NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.rename_group_title ) );

      if (!Strings.empty( dialog.name() ) )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeDescription( selected, dialog.name() );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );

      super.notifyTransactions( transactions );
   }
}
