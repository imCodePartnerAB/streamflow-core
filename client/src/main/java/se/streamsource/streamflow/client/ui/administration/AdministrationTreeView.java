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

package se.streamsource.streamflow.client.ui.administration;

import static java.util.Arrays.asList;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;

import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.util.Strings;

/**
 * JAVADOC
 */
public class AdministrationTreeView
      extends JPanel
   implements TransactionListener
{
   private JXTree tree;

   @Service
   DialogService dialogs;
   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   private AdministrationModel model;

   @Structure
   ValueBuilderFactory vbf;

   public AdministrationTreeView( @Service ApplicationContext context,
                                     @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf) throws Exception
   {
      super( new BorderLayout() );
      this.model = obf.newObjectBuilder( AdministrationModel.class ).use( client ).newInstance();
      tree = new JXTree( model );

      tree.setRootVisible( true );
      tree.setShowsRootHandles( true );

      DefaultTreeRenderer renderer = new DefaultTreeRenderer( new WrappingProvider(
            new IconValue()
            {
               public Icon getIcon( Object o )
               {
                  DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                  ContextItem clientInfo = (ContextItem) node.getUserObject();
                  return i18n.icon( Icons.valueOf(clientInfo.getRelation()));
               }
            },
            new StringValue()
            {
               public String getString( Object o )
               {
                  DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                  ContextItem clientInfo = (ContextItem) node.getUserObject();
                  return clientInfo.getName();
               }
            },
            false
      ) );
      tree.setCellRenderer( renderer );

      JPanel toolbar = new JPanel();
      toolbar.setBorder( BorderFactory.createEtchedBorder() );

      add( new JScrollPane(tree), BorderLayout.CENTER );

      final ActionMap am = context.getActionMap( this );

      JPopupMenu adminPopup = new JPopupMenu();
      adminPopup.add( am.get( "changeDescription" ) );
      adminPopup.add( am.get( "removeOrganizationalUnit" ) );
      adminPopup.add( new JSeparator() );
      adminPopup.add( am.get( "moveOrganizationalUnit" ) );
      adminPopup.add( am.get( "mergeOrganizationalUnit" ) );

      JPanel actions = new JPanel();
      actions.add(new JButton(am.get( "createOrganizationalUnit" )));
      actions.add(new JButton(new OptionsAction(adminPopup)));

      add( actions, BorderLayout.SOUTH);

      new RefreshWhenVisible( this, model );

      tree.getSelectionModel().addTreeSelectionListener( new SelectionActionEnabler(
            am.get( "changeDescription" ),
            am.get( "removeOrganizationalUnit" ),
            am.get( "moveOrganizationalUnit"),
            am.get( "mergeOrganizationalUnit"),
            am.get( "createOrganizationalUnit"))
      {
         @Override
         public boolean isSelectedValueValid( javax.swing.Action action )
         {
            // TODO This should be done by asking for possible interactions on server instead
/*

            Object node = tree.getLastSelectedPathComponent();

            String name = ((ApplicationAction) action).getName();
            if (asList( "moveOrganizationalUnit","mergeOrganizationalUnit","removeOrganizationalUnit").contains( name ))
            {
               return node instanceof OrganizationalUnitAdministrationNode;
            } else if ("changeDescription".equals( name ))
            {
               return !(node instanceof AccountAdministrationNode);
            } else if ("createOrganizationalUnit".equals( name ))
            {
               return !(node instanceof AccountAdministrationNode);
            }


*/

            return super.isSelectedValueValid( action );
         }
      });

   }


   public JTree getTree()
   {
      return tree;
   }

   @Action
   public Task changeDescription()
   {
      Object node = tree.getSelectionPath().getLastPathComponent();

      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.change_ou_title ) );
      if (Strings.notEmpty( dialog.name() ))
      {
         if (node instanceof MutableTreeNode)
         {
            DefaultMutableTreeNode orgNode = (DefaultMutableTreeNode) node;
            final ContextItem client = (ContextItem) orgNode.getUserObject();
            final ValueBuilder<se.streamsource.dci.value.StringValue> builder = vbf.newValueBuilder( se.streamsource.dci.value.StringValue.class );
            builder.prototype().string().set( dialog.name() );
            return new CommandTask()
            {
               @Override
               public void command()
                  throws Exception
               {
                  client.getClient().putCommand( "changedescription", builder.newInstance() );
               }
            };
         }
      }

      return null;
   }

   @Action
   public void createOrganizationalUnit()
   {
      Object node = tree.getSelectionPath().getLastPathComponent();

      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_ou_title ) );
      if (Strings.notEmpty( dialog.name() ))
      {
         ArrayList<Integer> expandedRows = new ArrayList<Integer>();
         for (int i = 0; i < tree.getRowCount(); i++)
         {
            if (tree.isExpanded( i ))
               expandedRows.add( i );
         }
         int[] selected = tree.getSelectionRows();

         model.createOrganizationalUnit( node, dialog.name() );

         for (Integer expandedRow : expandedRows)
         {
            tree.expandRow( expandedRow );
         }
         tree.setSelectionRows( selected );
      }
   }

   @Action
   public void removeOrganizationalUnit()
   {
      Object node = tree.getSelectionPath().getLastPathComponent();
/* TODO
      if (node instanceof OrganizationalUnitAdministrationNode)
      {
         OrganizationalUnitAdministrationNode orgNode = (OrganizationalUnitAdministrationNode) node;

         ConfirmationDialog dialog = confirmationDialog.iterator().next();
         dialog.setRemovalMessage( node.toString() );
         dialogs.showOkCancelHelpDialog( this, dialog, text( StreamflowResources.confirmation ) );
         if (dialog.isConfirmed())
         {
            ArrayList<Integer> expandedRows = new ArrayList<Integer>();
            for (int i = 0; i < tree.getRowCount(); i++)
            {
               if (tree.isExpanded( i ))
                  expandedRows.add(  i );
            }

            model.removeOrganizationalUnit( orgNode.getParent(), orgNode.ou().entity().get() );

            for (Integer expandedRow : expandedRows)
            {
               tree.expandRow( expandedRow );
            }
         }
      }
*/
   }

   @Action
   public void moveOrganizationalUnit()
   {
/* TODO
      OrganizationalUnitAdministrationNode moved =
            (OrganizationalUnitAdministrationNode) tree.getSelectionPath().getLastPathComponent();

      SelectOrganizationOrOrganizationalUnitDialog moveDialog = obf.newObjectBuilder( SelectOrganizationOrOrganizationalUnitDialog.class ).use( model ).newInstance();

      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), moveDialog, i18n.text( AdministrationResources.move_to ) );

      if ( moveDialog.target() != null )
      {
         moved.model().moveOrganizationalUnit( moveDialog.target() );
      }
*/

   }

   @Action
   public void mergeOrganizationalUnit()
   {
/* TODO
      OrganizationalUnitAdministrationNode moved =
            (OrganizationalUnitAdministrationNode) tree.getSelectionPath().getLastPathComponent();

      SelectOrganizationalUnitDialog mergeDialog = obf.newObjectBuilder( SelectOrganizationalUnitDialog.class ).use( model ).newInstance();

      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), mergeDialog, i18n.text( AdministrationResources.merge_to ) );

      if (mergeDialog.target() != null)
      {
         moved.model().mergeOrganizationalUnit( mergeDialog.target() );
      }
*/
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( transactions, Events.withNames("changedDescription", "removedOrganizationalUnit", "addedOrganizationalUnit" )))
         model.refresh();
   }
}