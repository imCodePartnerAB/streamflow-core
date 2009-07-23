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

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.users.shared.user.inbox.SharedUserInboxClientResource;
import se.streamsource.streamflow.client.ui.DetailView;

import javax.swing.JComponent;

/**
 * JAVADOC
 */
public class ProjectInboxNode
        extends DefaultMutableTreeTableNode
        implements DetailView
{
    @Service
    UserInboxView view;

    @Service
    UserInboxModel model;

    @Service
    ApplicationContext context;

    public ProjectInboxNode(@Uses SharedUserInboxClientResource inbox)
    {
        super(inbox, false);
    }

    @Override
    public Object getValueAt(int column)
    {
        return i18n.text(WorkspaceResources.inbox_node);
    }

    SharedUserInboxClientResource inbox()
    {
        return (SharedUserInboxClientResource) getUserObject();
    }

    public JComponent detailView()
    {
        context.getTaskService().execute(new Task(context.getApplication())
        {
            protected Object doInBackground() throws Exception
            {
                try
                {
                    model.setInbox(inbox());
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }

                return null;
            }
        });
        return view;
    }
}