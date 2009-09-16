/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.PopupMenuTrigger;
import se.streamsource.streamflow.infrastructure.event.source.*;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class AdministrationOutlineView
        extends JPanel
{
    final private JXTree tree;

    @Service
    DialogService dialogs;
    @Uses
    Iterable<NameDialog> nameDialogs;
    private AdministrationModel model;
    public EventSourceListener subscriber;

    public AdministrationOutlineView(@Service ApplicationContext context,
                                     @Uses final AdministrationModel model,
                                     @Service EventSource source) throws Exception
    {
        super(new BorderLayout());
        this.model = model;
        tree = new JXTree(model);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);

        subscriber = new EventSourceListener()
        {

            public void eventsAvailable(EventStore source, EventSpecification specification)
            {
                Logger.getLogger("administration").info("Refresh organizational overview");
                model.getRoot().refresh();
                model.reload(model.getRoot());
                tree.expandAll();
            }
        };
        source.registerListener(subscriber, new EventQuery().
                withNames("organizationalUnitMoved", "organizationalUnitMerged"));

        DefaultTreeRenderer renderer = new DefaultTreeRenderer(new WrappingProvider(
                new IconValue()
                {
                    public Icon getIcon(Object o)
                    {
                        if (o instanceof AccountAdministrationNode)
                            return i18n.icon(Icons.account, i18n.ICON_24);
                        else if (o instanceof OrganizationalStructureAdministrationNode)
                            return i18n.icon(Icons.organization, i18n.ICON_24);
                        else
                            return NULL_ICON;
                    }
                },
                new StringValue()
                {
                    public String getString(Object o)
                    {
                        if (o instanceof AdministrationNode)
                            return "                            ";
                        else if (o instanceof AccountAdministrationNode)
                            return ((AccountAdministrationNode) o).accountModel().settings().name().get();
                        else if (o instanceof OrganizationalStructureAdministrationNode)
                            return ((OrganizationalStructureAdministrationNode) o).toString();
                        else
                            return "Unknown";
                    }
                },
                false
        ));
        tree.setCellRenderer(renderer);

        JPanel toolbar = new JPanel();
        toolbar.setBorder(BorderFactory.createEtchedBorder());

        add(BorderLayout.CENTER, tree);

        ActionMap am = context.getActionMap(this);
        JPopupMenu popup = new JPopupMenu();
        popup.add(am.get("createOrganizationalUnit"));
        popup.add(am.get("removeOrganizationalUnit"));
        popup.add(am.get("moveOrganizationalUnit"));
        popup.add(am.get("mergeOrganizationalUnit"));

        tree.addMouseListener(new PopupMenuTrigger(popup));


        addAncestorListener(new AncestorListener()
        {
            public void ancestorAdded(AncestorEvent event)
            {
                model.refresh();
            }

            public void ancestorRemoved(AncestorEvent event)
            {
            }

            public void ancestorMoved(AncestorEvent event)
            {
            }
        });
    }


    public JTree getTree()
    {
        return tree;
    }

    @Action
    public void createOrganizationalUnit()
    {
        Object node = tree.getSelectionPath().getLastPathComponent();
        if (node instanceof OrganizationalStructureAdministrationNode)
        {
            OrganizationalStructureAdministrationNode orgNode = (OrganizationalStructureAdministrationNode) node;

            NameDialog dialog = nameDialogs.iterator().next();
            dialogs.showOkCancelHelpDialog(this, dialog);
            if (dialog.name() != null)
            {
                ArrayList<Integer> expandedRows = new ArrayList<Integer>();
                for (int i = 0; i < tree.getRowCount(); i++)
                {
                    if (tree.isExpanded(i))
                        expandedRows.add(i);
                }
                int[] selected = tree.getSelectionRows();

                model.createOrganizationalUnit(orgNode, dialog.name());

                model.refresh();
                for (Integer expandedRow : expandedRows)
                {
                    tree.expandRow(expandedRow);
                }
                tree.setSelectionRows(selected);
            }
        }
    }

    @Action
    public void removeOrganizationalUnit()
    {
        Object node = tree.getSelectionPath().getLastPathComponent();
        if (node instanceof OrganizationalStructureAdministrationNode)
        {
            OrganizationalStructureAdministrationNode orgNode = (OrganizationalStructureAdministrationNode) node;

            Object parent = orgNode.getParent();
            if (parent instanceof OrganizationalStructureAdministrationNode)
            {
                OrganizationalStructureAdministrationNode orgParent = (OrganizationalStructureAdministrationNode) parent;
                orgParent.model().removeOrganizationalUnit(orgNode.ou().entity().get());
                model.refresh();

            }
        }
    }

    @Action
    public void moveOrganizationalUnit()
    {
       System.out.println("Start move.");
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON);
        tree.setTransferHandler(new MoveTransferHandler());
    }

    @Action
    public void mergeOrganizationalUnit()
    {
       System.out.println("Start merge."); 
    }

    class MoveTransferHandler extends TransferHandler
    {

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public Transferable createTransferable(JComponent c) {
            return (OrganizationalStructureAdministrationNode)((JXTree)c).getSelectionPath().getLastPathComponent();
        }

        @Override
        public void exportDone(JComponent c, Transferable t, int action) {

            JXTree tree = (JXTree)c;
            tree.setDragEnabled(false);
            tree.setTransferHandler(null);

        }

        @Override
        public boolean canImport(TransferSupport transferSupport) {
            if("OrganizationalStructureNode".equals(transferSupport.getDataFlavors()[0].getHumanPresentableName()))
            {
                return true;
            }
            return false;
        }

        @Override
        public boolean importData(TransferSupport transferSupport) {
            System.out.println("Import start");
            try
            {
                OrganizationalStructureAdministrationNode movedNode =
                        (OrganizationalStructureAdministrationNode)tree.getSelectionPath().getLastPathComponent();

                OrganizationalStructureAdministrationNode dropNode =
                        (OrganizationalStructureAdministrationNode)tree.getDropLocation()
                                .getPath().getLastPathComponent();

                EntityReference from = (EntityReference)transferSupport.getTransferable().getTransferData(transferSupport.getDataFlavors()[0]);

                movedNode.model().moveOrganizationalUnit(from, dropNode.ou().entity().get());

                return true;

            } catch(Exception e)
            {
                throw new OperationException(AdministrationResources.could_not_move_organization, e);
            }
        }
    }
}
