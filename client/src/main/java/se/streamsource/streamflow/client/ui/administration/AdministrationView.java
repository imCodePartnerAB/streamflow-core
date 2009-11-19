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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationsTabbedView;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * JAVADOC
 */
public class AdministrationView
        extends JPanel
{
    @Structure
    ObjectBuilderFactory obf;

    @Structure
    UnitOfWorkFactory uowf;

    JSplitPane mainView = new JSplitPane();
    JPanel detailView;

    CardLayout viewSwitch = new CardLayout();

    public AdministrationView(@Uses AdministrationOutlineView adminOutlineView)
    {
        setLayout( viewSwitch );

        detailView = new JPanel(new BorderLayout());

        detailView.add( new JButton("Done"), BorderLayout.SOUTH );

        add(mainView, "main");
        add(detailView, "detail");

        viewSwitch.show( this, "main" );

        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(getMinimumSize());

        mainView.setOneTouchExpandable( true );

        mainView.setLeftComponent(adminOutlineView);
        adminOutlineView.setMinimumSize(new Dimension(200, 400));
        mainView.setRightComponent(new JPanel());

        mainView.setDividerLocation(200);
        mainView.setResizeWeight(0);
        adminOutlineView.getTree().addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                final TreePath path = e.getNewLeadSelectionPath();
                if (path != null)
                {
                    Object node = path.getLastPathComponent();

                    JComponent view = new JPanel();

                    if (node instanceof AccountAdministrationNode)
                    {
                        AccountAdministrationNode accountAdminNode = (AccountAdministrationNode) node;
                        boolean load = true;

                        try
                        {
                            // check if permissions are sufficient
                            accountAdminNode.accountModel().serverResource().organizations().users();
                        } catch(ResourceException re)
                        {
                            if(Status.CLIENT_ERROR_FORBIDDEN.equals(re.getStatus()))
                                load = false;
                        }                   

                        if(load)
                        {
                            view = obf.newObjectBuilder(OrganizationsTabbedView.class).use(
                                    accountAdminNode.organizationsModel(), accountAdminNode.usersModel())
                                    .newInstance();
                        }



                    } else if (node instanceof OrganizationalStructureAdministrationNode)
                    {
                        OrganizationalStructureAdministrationNode ouNode = (OrganizationalStructureAdministrationNode) node;
                        OrganizationalUnitAdministrationModel ouAdminModel = ouNode.model();
                        ouAdminModel.refresh();
                        view = obf.newObjectBuilder(OrganizationalUnitAdministrationView.class).use(ouAdminModel,
                                ouAdminModel.groupsModel(),
                                ouAdminModel.projectsModel(),
                                ouAdminModel.rolesModel(),
                                ouAdminModel.formsModel(),
                                ouAdminModel.administratorsModel(),
                                AdministrationView.this).newInstance();
                    }

                    mainView.setRightComponent(view);
                }
            }
        });
    }

    public void show(JComponent view)
    {
        detailView.add( view, BorderLayout.CENTER );
        viewSwitch.show( this, "detail" );
    }
}
