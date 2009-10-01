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

package se.streamsource.streamflow.client.resource.users.workspace.user.waitingfor;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.entity.EntityReference;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.users.workspace.TaskListClientResource;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskListDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

import java.util.List;

/**
 * JAVADOC
 */
public class UserWaitingForClientResource
        extends TaskListClientResource
{
    public UserWaitingForClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference, UserWaitingForTaskClientResource.class);
    }

    public List<WaitingForTaskDTO> tasks(TasksQuery query) throws ResourceException
    {
        return query("tasks", query, WaitingForTaskListDTO.class).<WaitingForTaskListDTO>buildWith().prototype().tasks().get();
    }

    public void reject(String taskId) throws ResourceException
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set(EntityReference.parseEntityReference(taskId));
        putCommand("reject", builder.newInstance());

    }

    public void completeFinishedTask(String taskId) throws ResourceException
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set(EntityReference.parseEntityReference(taskId));
        putCommand("completeFinishedTask", builder.newInstance());
    }

    public void completeWaitingForTask(String taskId) throws ResourceException
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set(EntityReference.parseEntityReference(taskId));
        putCommand("completeWaitingForTask", builder.newInstance());
    }

}
