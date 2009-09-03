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

package se.streamsource.streamflow.web.resource.users.overview.projects.assignments;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.assignment.OverviewAssignedTaskDTO;
import se.streamsource.streamflow.resource.assignment.OverviewAssignmentsTaskListDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * Mapped to:
 * /users/{user}/overview/projects/{project}/assignments
 */
public class OverviewProjectAssignmentsServerResource
        extends AbstractTaskListServerResource
{
    public OverviewAssignmentsTaskListDTO tasks(TasksQuery query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String projectId = (String) getRequest().getAttributes().get("project");

        // Find all Active tasks owned by "project"
        QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
        Association<Assignee> assignedTo = templateFor(Assignable.AssignableState.class).assignedTo();
        Property<String> ownerIdProp = templateFor(Ownable.OwnableState.class).owner().get().identity();
        queryBuilder.where(and(
                eq(ownerIdProp, projectId),
                isNotNull(assignedTo),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

        Query<TaskEntity> assignmentsQuery = queryBuilder.newQuery(uow);
        assignmentsQuery.orderBy(orderBy(templateFor(CreatedOn.CreatedOnState.class).createdOn()));

        return buildTaskList(assignmentsQuery, OverviewAssignedTaskDTO.class, OverviewAssignmentsTaskListDTO.class);
    }

    @Override
    protected <T extends TaskListDTO> void buildTask(TaskDTO prototype, ValueBuilder<ListItemValue> labelBuilder, ListItemValue labelPrototype, TaskEntity task)
    {
        OverviewAssignedTaskDTO taskDTO = (OverviewAssignedTaskDTO) prototype;
        Assignee assignee = task.assignedTo().get();
        if (assignee != null)
            taskDTO.assignedTo().set(assignee.getDescription());
        super.buildTask(prototype, labelBuilder, labelPrototype, task);
    }
}