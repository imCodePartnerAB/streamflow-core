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
package se.streamsource.streamflow.client.ui.administration.roles;

import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;

import se.streamsource.streamflow.client.util.StreamflowButton;
import javax.swing.JList;
import javax.swing.JPanel;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;
import ca.odell.glazedlists.swing.EventListModel;

/**
 * JAVADOC
 */
public class RolesView
      extends JPanel
   implements TransactionListener
{
   RolesModel model;

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   public JList roleList;

   public RolesView( @Service ApplicationContext context, @Uses final RolesModel model )
   {
      super( new BorderLayout() );
      this.model = model;

      setActionMap( context.getActionMap( this ) );

      roleList = new JList( new EventListModel<LinkValue>(model.getList()) );

      roleList.setCellRenderer( new LinkListCellRenderer() );
      add( roleList, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new StreamflowButton( getActionMap().get( "add" ) ) );
      toolbar.add( new StreamflowButton( getActionMap().get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      new RefreshWhenShowing( this, model );
   }

   @Action
   public Task add()
   {
      NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_role_title ) );
      final String name = dialog.name();
      if ( !Strings.empty( name ) )
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
      final LinkValue selected = (LinkValue) roleList.getSelectedValue();

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
               model.remove(selected);
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions);
   }
}