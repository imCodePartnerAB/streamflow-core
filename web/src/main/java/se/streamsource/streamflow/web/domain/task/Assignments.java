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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.entity.association.ManyAssociation;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Assignments.AssignmentsMixin.class)
public interface Assignments
{
    Task createAssignedTask(Assignee assignee);

    void completeAssignedTask(Task task);

    void dropAssignedTask(Task task);

    void delegateAssignedTaskTo(Task task, Delegatee delegatee);

    void forwardAssignedTask(Task task, Inbox receiverInbox);

    void markAssignedTaskAsRead(Task task);

    void markAssignedTaskAsUnread(Task task);

    interface AssignmentsState
    {
        void assignedTaskMarkedAsRead(DomainEvent event, Task task);
        void assignedTaskMarkedAsUnread(DomainEvent event, Task task);
        ManyAssociation<Task> unreadAssignedTasks();
    }



    abstract class AssignmentsMixin
            implements Assignments, AssignmentsState
    {
        @Structure
        UnitOfWorkFactory uowf;

        @This
        Owner owner;

        @This
        WaitingFor waitingFor;

        @This
        Inbox inbox;

        public Task createAssignedTask(Assignee assignee)
        {
            Task task = inbox.createTask();
            task.assignTo(assignee);
            return task;
        }

        public void completeAssignedTask(Task task)
        {
            task.complete();
            markAssignedTaskAsRead(task);
        }

        public void dropAssignedTask(Task task)
        {
            task.drop();
            markAssignedTaskAsRead(task);
        }

        public void delegateAssignedTaskTo(Task task, Delegatee delegatee)
        {
            Assignable.AssignableState assignable = (Assignable.AssignableState) task;
            Delegator delegator = (Delegator) assignable.assignedTo().get();
            task.unassign();
            task.delegateTo(delegatee, delegator, waitingFor);
        }

        public void forwardAssignedTask(Task task, Inbox receiverInbox)
        {
            receiverInbox.receiveTask(task);
        }

        public void markAssignedTaskAsRead(Task task)
        {
            if (!unreadAssignedTasks().contains(task))
            {
                return;
            }
            assignedTaskMarkedAsRead(DomainEvent.CREATE, task);
        }

        public void markAssignedTaskAsUnread(Task task)
        {
            if (unreadAssignedTasks().contains(task))
            {
                return;
            }
            assignedTaskMarkedAsUnread(DomainEvent.CREATE, task);
        }

        public void assignedTaskMarkedAsRead(DomainEvent event, Task task)
        {
            unreadAssignedTasks().remove(task);
        }

        public void assignedTaskMarkedAsUnread(DomainEvent event, Task task)
        {
            unreadAssignedTasks().add(task);
        }
    }
}
