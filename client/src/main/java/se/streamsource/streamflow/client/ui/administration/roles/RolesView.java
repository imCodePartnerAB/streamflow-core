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

package se.streamsource.streamflow.client.ui.administration.roles;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class RolesView
        extends JPanel
{
    RolesModel model;

    @Service
    DialogService dialogs;

    @Uses
    Iterable<NameDialog> nameDialogs;

    public JList projectList;

    public RolesView(@Service ApplicationContext context, @Uses final RolesModel model)
    {
        super(new BorderLayout());
        this.model = model;

        setActionMap(context.getActionMap(this));

        projectList = new JList(model);

        projectList.setCellRenderer(new ListItemCellRenderer());
        add(projectList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(getActionMap().get("add")));
        toolbar.add(new JButton(getActionMap().get("remove")));
        add(toolbar, BorderLayout.SOUTH);
    }

    @Action
    public void add()
    {
        NameDialog dialog = nameDialogs.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog);
        String name = dialog.name();
        if (name != null)
        {
            model.newRole(name);
        }
    }

    @Action
    public void remove()
    {
        ListItemValue selected = (ListItemValue) projectList.getSelectedValue();
        model.removeRole(selected.entity().get().identity());
    }

    public JList getProjectList()
    {
        return projectList;
    }
}