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

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Inbox.InboxMixin.class)
public interface Inbox
{
    Task createTask();

    void receiveTask(Task task);

    void completeTask(Task task, Assignee assignee);

    void dropTask(Task task, Assignee assignee);

    void assignTo(Task task, Assignee assignee);

    void delegateTo(Task task, Delegatee delegatee, Delegator delegator);

    void markAsRead(Task task);

    void markAsUnread(Task task);

    void deleteTask( Task task );

    interface Data
    {
        Task createdTask(DomainEvent event, String id);
        void deletedTask(DomainEvent event, Task task);
        void markedAsRead(DomainEvent event, Task task);
        void markedAsUnread(DomainEvent event, Task task);
        ManyAssociation<Task> unreadInboxTasks();
    }

    abstract class InboxMixin
            implements Inbox, Data
    {
        @This
        Owner owner;

        @This
        WaitingFor waitingFor;

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        UnitOfWorkFactory uowf;

        @Service
        IdentityGenerator idGenerator;

        public Task createTask()
        {
            TaskEntity taskEntity = (TaskEntity) createdTask(DomainEvent.CREATE, idGenerator.generate(TaskEntity.class));
            taskEntity.changeOwner(owner);
            taskEntity.addContact(vbf.newValue(ContactValue.class));

            return taskEntity;
        }

        public void receiveTask(Task task)
        {
            task.unassign();
            task.changeOwner(owner);
            markAsUnread(task);
        }

        public void completeTask(Task task, Assignee assignee)
        {
            task.assignTo(assignee);
            task.complete();
        }

        public void dropTask(Task task, Assignee assignee)
        {
            task.assignTo(assignee);
            task.drop();
        }

        public void assignTo(Task task, Assignee assignee)
        {
            task.assignTo(assignee);
        }

        public void delegateTo(Task task, Delegatee delegatee, Delegator delegator)
        {
            task.delegateTo(delegatee, delegator, waitingFor);
        }

        public void markAsRead(Task task)
        {
            if (!unreadInboxTasks().contains(task))
            {
                return;
            }
            markedAsRead(DomainEvent.CREATE, task);
        }

        public void markAsUnread(Task task)
        {
            if (unreadInboxTasks().contains(task))
            {
                return;
            }
            markedAsUnread(DomainEvent.CREATE, task);
        }

        public Task createdTask(DomainEvent event, String id)
        {
            EntityBuilder<TaskEntity> builder = uowf.currentUnitOfWork().newEntityBuilder(TaskEntity.class, id);
            builder.instance().createdOn().set( event.on().get() );
            return builder.newInstance();
        }

        public void deleteTask( Task task )
        {
            if (((TaskStatus.Data)task).status().get().equals(TaskStates.ACTIVE))
            {
                markAsRead( task );
                deletedTask( DomainEvent.CREATE, task );
            }
        }

        public void deletedTask( DomainEvent event, Task task )
        {
            uowf.currentUnitOfWork().remove( task );
        }

        public void markedAsRead(DomainEvent event, Task task)
        {
            unreadInboxTasks().remove(task);
        }

        public void markedAsUnread(DomainEvent event, Task task)
        {
            unreadInboxTasks().add(task);
        }
    }
}
