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

package se.streamsource.streamflow.web.domain.entity.organization;

import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.form.FieldDefinitionsQueries;
import se.streamsource.streamflow.web.domain.entity.form.FormQueries;
import se.streamsource.streamflow.web.domain.entity.label.PossibleLabelsQueries;
import se.streamsource.streamflow.web.domain.entity.tasktype.TaskTypesQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.IdGenerator;
import se.streamsource.streamflow.web.domain.structure.form.FieldTemplates;
import se.streamsource.streamflow.web.domain.structure.form.FormTemplates;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.domain.structure.project.ProjectRoles;
import se.streamsource.streamflow.web.domain.structure.role.Roles;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskTypes;

/**
 * A root organization.
 */
@Mixins(OrganizationEntity.LifecycleConcern.class)
public interface OrganizationEntity
      extends DomainEntity,

      // Interactions
      IdGenerator,
      IdGenerator.Data,

      // Structure
      Organization,

      // Data
      Describable.Data,
      FieldTemplates.Data,
      FormTemplates.Data,
      Labels.Data,
      OrganizationalUnits.Data,
      OwningOrganization,
      ProjectRoles.Data,
      Removable.Data,
      RolePolicy.Data,
      Roles.Data,
      PermissionQueries,
      SelectedLabels.Data,
      TaskTypes.Data,

      //Queries
      FieldDefinitionsQueries,
      FormQueries,
      OrganizationParticipationsQueries,
      OrganizationQueries,
      OrganizationalUnitsQueries,
      PossibleLabelsQueries,
      TaskTypesQueries
{
   abstract class LifecycleConcern
         extends OrganizationalUnitRefactoring.Mixin
         implements Lifecycle, OwningOrganization
   {
      @This
      Organization org;

      public void create() throws LifecycleException
      {
         organization().set( org );
      }

      public void remove() throws LifecycleException
      {
      }
   }
}
