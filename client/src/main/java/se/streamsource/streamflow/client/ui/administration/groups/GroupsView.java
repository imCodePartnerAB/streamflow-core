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
package se.streamsource.streamflow.client.ui.administration.groups;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.Links;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import java.awt.Component;

import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class GroupsView
      extends ListDetailView
{
   @Structure
   Module module;

   private GroupsModel model;

   @Service
   private DialogService dialogs;

   public GroupsView( @Service ApplicationContext context, @Uses final GroupsModel model)
   {
      this.model = model;

      final ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("create"), new javax.swing.Action[]{am.get( "rename" ), am.get( "remove" )},
            new DetailFactory()
            {
               public Component createDetail( LinkValue detailLink )
               {
                  final GroupModel groupModel = (GroupModel) model.newResourceModel( detailLink );
                  groupModel.refresh();
                  Iterable<LinkValue> participants1 = Iterables.filter( Links.withRel( "participants" ), (Iterable<LinkValue>) groupModel.getResources() );
                  LinkValue participants = Iterables.first( participants1 );

                  return module.objectBuilderFactory().newObjectBuilder( ParticipantsView.class ).use( groupModel.newResourceModel( participants ) ).newInstance();
               }
            }
      );

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task create()
   {
      NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);
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

      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
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

      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);
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

      if( matches( withNames("removedParticipant"), transactions )
            && matches( withNames( "removedGroup" ),transactions) )
      {
         list.clearSelection();
      }
   }
}
