/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.DeleteContext;
import se.streamsource.dci.context.InteractionConstraints;
import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.task.PossibleActions;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.entity.task.TaskTypeQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresAssigned;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresDelegated;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresOwner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.List;

import static se.streamsource.streamflow.domain.interaction.gtd.States.*;

/**
 * JAVADOC
 */
@Mixins(TaskActionsContext.Mixin.class)
public interface TaskActionsContext
      extends DeleteContext // , InteractionConstraints
{
   // List possible actions
   public Actions actions();

   public LinksValue possiblesendtoprojects();

   @RequiresOwner(User.class)
   public LinksValue possiblesendtousers();

   public LinksValue possibledelegateprojects();

   @RequiresOwner(User.class)
   public LinksValue possibledelegateusers();

   // Commands

   /**
    * Accept a delegated task.
    */
   @RequiresDelegated(true)
   public void accept();

   /**
    * Assign the task to the user invoking the method
    */
   @RequiresAssigned(false)
   public void assign();

   /**
    * Mark the task as completed
    */
   @RequiresStatus({ACTIVE, DONE})
   public void complete();

   /**
    * Mark the task as done.
    */
   @RequiresStatus({ACTIVE})
   public void done();

   public void onhold();

   public void sendto( EntityReferenceDTO entity );

   @RequiresDelegated(false)
   public void delegate( EntityReferenceDTO entity );

   @RequiresStatus({ACTIVE, DONE})
   public void drop();

   @RequiresStatus({COMPLETED, DROPPED})
   public void reactivate();

   @RequiresStatus({DONE})
   public void redo();

   @RequiresDelegated(true)
   public void reject();

   @RequiresStatus({States.ON_HOLD})
   public void resume();

   @RequiresAssigned(true)
   public void unassign();

   public void delete();

   abstract class Mixin
         extends ContextMixin
         implements TaskActionsContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      // List possible actions
      public Actions actions()
      {
         ValueBuilder<Actions> builder = vbf.newValueBuilder( se.streamsource.streamflow.domain.interaction.gtd.Actions.class );
         List<String> actions = builder.prototype().actions().get();

         PossibleActions possibleActions = context.role( PossibleActions.class );
         Actor actor = context.role(Actor.class);

         possibleActions.addActions( actor, actions );

         return builder.newInstance();
      }

      public LinksValue possiblesendtoprojects()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "sendto" );
         List<Project> projects = context.role( TaskTypeQueries.class ).possibleProjects();
         Ownable ownable = context.role(Ownable.class);
         TaskType taskType = context.role( TypedTask.Data.class).taskType().get();
         for (Project project : projects)
         {
            if (!ownable.isOwnedBy( (Owner) project ))
            {
               if (taskType == null || project.hasSelectedTaskType( taskType ))
                  builder.addDescribable( project, ((OwningOrganizationalUnit.Data)project).organizationalUnit().get() );
            }
         }
         return builder.newLinks();
      }

      public LinksValue possiblesendtousers()
      {
         List<User> users = context.role( TaskTypeQueries.class ).possibleUsers();

         LinksBuilder links = new LinksBuilder(module.valueBuilderFactory()).command( "sendto" );
         Ownable ownable = context.role(Ownable.class);

         for (User user : users)
         {
            if (!ownable.isOwnedBy( (Owner) user ))
            {
               String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
               links.addDescribable( user, group );
            }
         }

         return links.newLinks();
      }

      public LinksValue possibledelegateprojects()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "delegate" );
         List<Project> projects = context.role( TaskTypeQueries.class ).possibleProjects();
         TaskType taskType = context.role( TypedTask.Data.class).taskType().get();
         Ownable ownable = context.role(Ownable.class);
         for (Project project : projects)
         {
            if (!ownable.isOwnedBy( (Owner) project ))
            {
               if (taskType == null || project.hasSelectedTaskType( taskType ))
                  builder.addDescribable( project, ((OwningOrganizationalUnit.Data)project).organizationalUnit().get() );
            }
         }
         return builder.newLinks();
      }

      public LinksValue possibledelegateusers()
      {
         List<User> users = context.role( TaskTypeQueries.class ).possibleUsers();

         LinksBuilder links = new LinksBuilder(module.valueBuilderFactory()).command( "delegate" );

         Ownable ownable = context.role(Ownable.class);
         for (User user : users)
         {
            if (!ownable.isOwnedBy( (Owner) user ))
            {
               String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
               links.addDescribable( user, group );
            }
         }

         return links.newLinks();
      }

      // Commands
      public void accept()
      {
         Assignable assignable = context.role( Assignable.class );
         Ownable ownable = context.role( Ownable.class );
         Delegatable.Data delegatable = context.role( Delegatable.Data.class );

         Actor actor = context.role(Actor.class);

         if (!assignable.isAssigned())
         {
            // Delegations
            Owner owner = (Owner) delegatable.delegatedTo().get();
            assignable.assignTo( actor );
            ownable.sendTo( owner );
         }
      }

      public void assign()
      {
         TaskEntity task = context.role(TaskEntity.class);

         Assignee assignee = context.role(Actor.class);

         if (!task.isAssigned())
         {
            task.assignTo( assignee );
         }

         /*if ( task.isDelegated() )
         {
            task.sendTo( (Owner) task.delegatedTo().get() );
         }*/
      }

      public void complete()
      {
         TaskEntity task = context.role(TaskEntity.class);

         Owner owner = task.owner().get();

         Actor actor = context.role(Actor.class);

         if (!task.isAssigned())
         {
            // Inbox or WaitingFor
            if (task.isDelegatedBy( actor ))
            {
               task.sendTo( owner );

            }

            task.assignTo( actor );
         }

         task.complete();
      }

      public void done()
      {
         TaskEntity task = context.role(TaskEntity.class);

         task.done();
      }

      public void onhold()
      {
         context.role(Status.class).onHold();
      }

      public void sendto( EntityReferenceDTO entity )
      {
         TaskEntity task = context.role(TaskEntity.class);

         Owner toOwner = uowf.currentUnitOfWork().get( Owner.class, entity.entity().get().identity() );

         task.unassign();

         task.sendTo( toOwner );
      }

      public void delegate( EntityReferenceDTO entity )
      {
         TaskEntity task = context.role(TaskEntity.class);

         Delegatee to = uowf.currentUnitOfWork().get( Delegatee.class, entity.entity().get().identity() );

         Owner owner = task.owner().get();

         Actor actor = context.role(Actor.class);

         if (task.isAssigned())
            task.unassign();

         task.delegateTo( to, actor, owner );
      }

      public void drop()
      {
         TaskEntity task = context.role(TaskEntity.class);

         Actor actor = context.role(Actor.class);

         if (!task.isAssigned())
         {
            task.assignTo( actor );
         }

         task.drop();
      }

      public void reactivate()
      {
         Status task = context.role(TaskEntity.class);
         task.reactivate();
      }

      public void redo()
      {
         Status task = context.role(TaskEntity.class);
         task.redo();
      }

      public void reject()
      {
         TaskEntity task = context.role(TaskEntity.class);

         task.rejectDelegation();
      }

      public void resume()
      {
         context.role(Status.class).resume();
      }

      public void unassign()
      {
         Assignable task = context.role(TaskEntity.class);

         task.unassign();
      }

      public void delete()
      {
         Removable task = context.role(TaskEntity.class);
         task.deleteEntity();
      }
   }
}