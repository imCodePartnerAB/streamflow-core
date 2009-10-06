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

package se.streamsource.streamflow.web.resource.users.workspace.user.assignments;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.assignment.AssignedTaskDTO;
import se.streamsource.streamflow.resource.assignment.AssignmentsTaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/user/assignments
 */
public class UserAssignmentsServerResource
        extends AbstractTaskListServerResource
{
    public AssignmentsTaskListDTO tasks(TasksQuery query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        Assignments assignments = uow.get(Assignments.class, id);

        // Find all my Active tasks assigned to "me"
        QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
        Association<Assignee> assignedId = templateFor(Assignable.AssignableState.class).assignedTo();
        Property<String> ownedId = templateFor(Ownable.OwnableState.class).owner().get().identity();
        Query<TaskEntity> assignmentsQuery = queryBuilder.where(and(
                eq(assignedId, uow.get(Assignee.class, id)),
                eq(ownedId, id),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE))).
                newQuery(uow);
        assignmentsQuery.orderBy(orderBy(templateFor(CreatedOn.class).createdOn()));

        return buildTaskList(assignmentsQuery, AssignedTaskDTO.class, AssignmentsTaskListDTO.class);
    }

    public void createtask()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("user");
        Assignments assignments = uow.get(Assignments.class, id);
        Assignee assignee = uow.get(Assignee.class, id);

        Task task = assignments.createAssignedTask(assignee);
    }
}