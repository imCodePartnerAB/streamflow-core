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
import static se.streamsource.streamflow.domain.task.TaskStates.*;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(WaitingFor.Mixin.class)
public interface WaitingFor
{
   void completeWaitingForTask( Task task, Assignee assignee );

   void completeFinishedTask( @HasStatus(DONE) Task task );

   void rejectFinishedTask( @HasStatus(DONE) Task task );

   void dropWaitingForTask( Task task, Assignee assignee );

   void assignWaitingForTask( @HasStatus(ACTIVE) Task task, Assignee assignee );

   void redoFinishedTask( TaskEntity task );

   void rejectTask( Task task );

   void deleteWaitingForTask( Task task );


   interface Data
   {
      void deletedWaitingForTask( DomainEvent event, Task task );
   }


   abstract class Mixin
         implements WaitingFor, Data
   {
      @Structure
      UnitOfWorkFactory uowf;

      @This
      Owner owner;

      @This
      Inbox inbox;

      public void completeWaitingForTask( Task task, Assignee assignee )
      {
         task.changeOwner( owner );
         task.assignTo( assignee );
         task.complete();
      }

      public void completeFinishedTask( Task task )
      {
         task.complete();
      }

      public void rejectFinishedTask( Task task )
      {
         task.activate();
      }

      public void dropWaitingForTask( Task task, Assignee assignee )
      {
         task.changeOwner( owner );
         task.assignTo( assignee );
         task.drop();
      }

      public void assignWaitingForTask( Task task, Assignee assignee )
      {
         task.unassign();
         task.rejectDelegation();
         task.changeOwner( owner );
         task.assignTo( assignee );
      }

      public void redoFinishedTask( TaskEntity task )
      {
         task.redo();
      }

      public void rejectTask( Task task )
      {
         inbox.receiveTask( task );
      }

      public void deleteWaitingForTask( Task task )
      {
         if (((TaskStatus.Data) task).status().get().equals( ACTIVE ))
         {
            deletedWaitingForTask( DomainEvent.CREATE, task );
         }
      }

      public void deletedWaitingForTask( DomainEvent event, Task task )
      {
         uowf.currentUnitOfWork().remove( task );
      }
   }
}
