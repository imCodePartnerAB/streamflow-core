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

package se.streamsource.streamflow.client.ui.administration.projects;

import java.awt.BorderLayout;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;

/**
 * JAVADOC
 */
public class ProjectMembersView
      extends JPanel
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Uses
   ObjectBuilder<SelectUsersAndGroupsDialog> selectUsersAndGroups;

   private UsersAndGroupsModel usersAndGroupsModel;



   public JXList membersList;
   private ProjectMembersModel membersModel;

   public ProjectMembersView( @Service ApplicationContext context,
                              @Uses final ProjectMembersModel membersModel,
                              @Structure ObjectBuilderFactory obf)
   {
      super( new BorderLayout() );
      this.membersModel = membersModel;
      ObjectBuilderFactory obf1 = obf;
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      usersAndGroupsModel = obf.newObjectBuilder( UsersAndGroupsModel.class ).use( membersModel.getFilterResource() ).newInstance();
      setActionMap( context.getActionMap( this ) );

      membersList = new JXList( new EventListModel<LinkValue>(new SortedList<LinkValue>(membersModel.getMembers(), new LinkComparator())) );

      membersList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane(membersList), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( getActionMap().get( "add" ) ) );
      toolbar.add( new JButton( getActionMap().get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
      membersList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( getActionMap().get( "remove" ) ) );


      addAncestorListener( new RefreshWhenVisible( membersModel, this ) );
   }


   @Action
   public void add()
   {
      SelectUsersAndGroupsDialog dialog = selectUsersAndGroups.use( usersAndGroupsModel ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text(AdministrationResources.add_user_or_group_title) );

      Set<LinkValue> linkValueSet = dialog.getSelectedEntities();
      if ( !linkValueSet.isEmpty() )
      {
         membersModel.addMembers( linkValueSet );
         membersModel.refresh();
      }
   }

   @Action
   public void remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed() && !membersList.isSelectionEmpty())
      {
         membersModel.removeMember( (LinkValue) membersList.getSelectedValue() );
         membersModel.refresh();
      }
   }
}