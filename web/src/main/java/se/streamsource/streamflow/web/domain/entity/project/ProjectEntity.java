/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.entity.project;

import org.qi4j.api.concern.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.query.*;
import org.qi4j.api.sideeffect.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.web.domain.entity.*;
import se.streamsource.streamflow.web.domain.entity.gtd.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.form.*;
import se.streamsource.streamflow.web.domain.structure.label.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.*;

/**
 * JAVADOC
 */
@SideEffects(ProjectEntity.RemoveMemberSideEffect.class)
@Mixins({ProjectEntity.ProjectIdGeneratorMixin.class})
@Concerns(ProjectEntity.RemovableConcern.class)
public interface ProjectEntity
        extends DomainEntity,

        // Interactions
        IdGenerator,

        // Structure
        Members,
        Project,
        OwningOrganizationalUnit,

        // Data
        CaseAccessDefaults.Data,
        Members.Data,
        Describable.Data,
        OwningOrganizationalUnit.Data,
        Ownable.Data,
        Forms.Data,
        Labels.Data,
        SelectedLabels.Data,
        CaseTypes.Data,
        Removable.Data,
        SelectedCaseTypes.Data,

        // Queries
        AssignmentsQueries,
        InboxQueries,
        ProjectLabelsQueries
{
   class ProjectIdGeneratorMixin
           implements IdGenerator
   {
      @This
      OwningOrganizationalUnit.Data state;

      public void assignId(CaseId aCase)
      {
         Organization organization = ((OwningOrganization) state.organizationalUnit().get()).organization().get();
         ((IdGenerator) organization).assignId(aCase);
      }
   }

   abstract class RemoveMemberSideEffect
           extends SideEffectOf<Members>
           implements Members
   {
      @This
      AssignmentsQueries assignments;

      @Structure
      UnitOfWorkFactory uowf;

      public void removeMember(Member member)
      {
         // Get all active cases in a project for a particular user and unassign.
         for (Assignable caze : assignments.assignments((Assignee) member, null).newQuery(uowf.currentUnitOfWork()))
         {
            caze.unassign();
         }
      }
   }

   abstract class RemovableConcern
           extends ConcernOf<Removable>
           implements Removable
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      @This
      Identity id;

      @This
      Members members;

      @This
      InboxQueries inbox;

      @This
      AssignmentsQueries assignments;

      public boolean removeEntity()
      {
         if (inbox.inboxHasActiveCases()
                 || assignments.assignmentsHaveActiveCases())
         {
            throw new IllegalStateException("Cannot remove project with OPEN cases.");

         } else
         {
            members.removeAllMembers();
            return next.removeEntity();
         }
      }
   }
}
