/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.workspace;

import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.task.TaskTableView;

import javax.swing.*;

/**
 * JAVADOC
 */
public class WorkspaceProjectDelegationsView
        extends TaskTableView
{

    protected void buildPopupMenu(JPopupMenu popup)
    {
        ActionMap am = getActionMap();
        Action markTasksAsUnread = am.get("markTasksAsUnread");
        popup.add(markTasksAsUnread);
        popup.add(am.get("markTasksAsRead"));
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(markTasksAsUnread));
    }

    @Override
    protected void buildToolbar(JPanel toolbar)
    {
        Action assignAction = addToolbarButton(toolbar, "assignTasksToMe");
        Action delegateTasksFromInbox = addToolbarButton(toolbar, "reject");
        addToolbarButton(toolbar, "refresh");
        taskTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(assignAction, delegateTasksFromInbox));
    }

    @org.jdesktop.application.Action
    public void reject() throws ResourceException
    {
        WorkspaceProjectDelegationsModel delegationsModel = (WorkspaceProjectDelegationsModel) model;
        for (int row : getReverseSelectedTasks())
        {
            delegationsModel.reject(row);
        }
        model.refresh();
    }
}