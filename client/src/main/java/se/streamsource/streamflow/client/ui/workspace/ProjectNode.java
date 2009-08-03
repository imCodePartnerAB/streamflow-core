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

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.users.workspace.projects.WorkspaceProjectClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.ProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.ProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.ProjectInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.ProjectWaitingforClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;

/**
 * JAVADOC
 */
public class ProjectNode
        extends DefaultMutableTreeTableNode
{
    private LabelsModel labelsModel;

    @Uses String projectName;

    public ProjectNode(@Uses WorkspaceProjectClientResource workspaceProjectClientResource,
                             @Structure ObjectBuilderFactory obf)
    {
        super(workspaceProjectClientResource);

        ProjectInboxClientResource projectInboxClientResource = workspaceProjectClientResource.inbox();
        add(obf.newObjectBuilder(ProjectInboxNode.class).use(projectInboxClientResource).newInstance());

        ProjectAssignmentsClientResource projectAssignmentsClientResource = workspaceProjectClientResource.assignments();
        add(obf.newObjectBuilder(ProjectAssignmentsNode.class).use(projectAssignmentsClientResource).newInstance());

        ProjectDelegationsClientResource projectDelegationsClientResource = workspaceProjectClientResource.delegations();
        add(obf.newObjectBuilder(ProjectDelegationsNode.class).use(projectDelegationsClientResource).newInstance());

        ProjectWaitingforClientResource projectWaitingforClientResource = workspaceProjectClientResource.waitingFor();
        add(obf.newObjectBuilder(ProjectWaitingForNode.class).use(projectWaitingforClientResource).newInstance());

        labelsModel = obf.newObjectBuilder(LabelsModel.class).use(workspaceProjectClientResource.labels()).newInstance();
    }

    @Override
    public Object getValueAt(int column)
    {
        return projectName;
    }

    @Override
    public ProjectsNode getParent()
    {
        return (ProjectsNode) super.getParent();
    }

    public LabelsModel labelsModel()
    {
        return labelsModel;
    }

    public ListValue findUsers(String name) throws ResourceException
    {
        return ((WorkspaceProjectClientResource)getUserObject()).findUsers(name);
    }

    public ListValue findProjects(String name) throws ResourceException
    {
        return ((WorkspaceProjectClientResource)getUserObject()).findProjects(name);
    }
}