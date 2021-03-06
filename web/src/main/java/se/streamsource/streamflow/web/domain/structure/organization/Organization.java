/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.organization;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessDefaults;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.CasePdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCases;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinitions;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroups;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.project.ProjectRoles;
import se.streamsource.streamflow.web.domain.structure.role.Roles;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUsers;

/**
 * JAVADOC
 */
public interface Organization
      extends
      Describable,
      ProjectRoles,
      RolePolicy,
      Forms,
      FormOnRemove,
      Labels,
      OrganizationalUnits,
      Owner,
      Roles,
      SelectedLabels,
      CaseTypes,
      Removable,
      AccessPoints,
      EmailAccessPoints,
      ProxyUsers,
      Attachments,
      DefaultPdfTemplate,
      FormPdfTemplate,
      CasePdfTemplate,
      DatatypeDefinitions,
      FieldGroups,
      Priorities,
      CaseAccessDefaults,
      Groups,
      ShadowCases,
      IntegrationPoints
{
}
